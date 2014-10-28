/*
 *  INTEL CONFIDENTIAL
 *
 *  Copyright (C) 2012-2013, Intel Corporation. All Rights Reserved.
 *
 *  The source code contained or described herein and all documents related to the source code
 *  ("Material") are owned by Intel Corporation or its suppliers or licensors. Title to the Material
 *  remains with Intel Corporation or its suppliers and licensors. The Material contains trade
 *  secrets and proprietary and confidential information of Intel or its suppliers and licensors.
 *  The Material is protected by worldwide copyright and trade secret laws and treaty provisions.
 *  No part of the Material may be used, copied, reproduced, modified, published, uploaded, posted,
 *  transmitted, distributed, or disclosed in any way without Intel`s prior express written
 *  permission.
 *
 *  No license under any patent, copyright, trade secret or other intellectual property right is
 *  granted to or conferred upon you by disclosure or delivery of the Materials, either expressly,
 *  by implication, inducement, estoppel or otherwise. Any license under such intellectual property
 *  rights must be express and approved by Intel in writing.
 */

package com.intel.commons;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

/**
 * Internal, safe and standard implementation of communication with web servers, used by the
 * {@link WebLoader} class. Supports both HTTP and HTTPS, optional basic authentication,
 * optional certificate pinning, and configurable timeouts.
 */
final class InternalWebLoader extends AsyncTaskLoader<HttpResponse>
{
    static final String KEY_URI         = "URI";
    static final String KEY_USERNAME    = "USERNAME";
    static final String KEY_PASSWORD    = "PASSWORD";
    static final String KEY_ISSUER      = "ISSUER";
    static final String KEY_COMMON_NAME = "COMMON_NAME";
    static final String KEY_TIMEOUT     = "TIMEOUT";

    private static final int BUFFER_SIZE = 4096; // 4 KB

    private static final int AUTH_RETRIES = 1;

    private int mAuthRetries;
    private Bundle mArgs;

    public InternalWebLoader(Context context, Bundle args)
    {
        super(context);
        mArgs = args;
    }

    @Override
    protected void onStartLoading()
    {
        // No cache to reuse or resources to clear
        super.onStartLoading();
        forceLoad();
    }

    @Override
    protected void onStopLoading()
    {
        // No cache to reuse or resources to clear
        super.onStopLoading();
        cancelLoad();
    }

    @Override
    protected void onReset()
    {
        // No cache to reuse or resources to clear
        super.onReset();
        cancelLoad();
    }

    @Override
    public HttpResponse loadInBackground()
    {
        HttpURLConnection connection = null;

        // Sanity check
        if (!isConnected())
        {
            return null; // Error
        }

        connection = sendRequest();
        if (connection == null)
        {
            return null; // Error
        }

        return receiveResponse(connection);
    }

    /**
     * Checks whether the device is currently connected to any network (e.g. cellular, Wi-Fi).
     * <p> 
     * Requires the permission {@link android.Manifest.permission.ACCESS_NETWORK_STATE}.
     */
    private boolean isConnected()
    {
        final ConnectivityManager connectivity = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo network = connectivity.getActiveNetworkInfo();

        return (network != null && network.isConnected());
    }

    /**
     * Sends an HTTP/S request.
     * <p>
     * Requires the permission {@link android.Manifest.permission.INTERNET}.
     *
     * @return {@code null} in case of any errors
     *
     * @see http://android-developers.blogspot.co.il/2011/09/androids-http-clients.html
     */
    private HttpURLConnection sendRequest()
    {
        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection) new URL(mArgs.getString(InternalWebLoader.KEY_URI))
                    .openConnection();

            // TODO: support other methods than the default "GET" with "connection.setRequestMethod(...)"
            configureAuthentication();
            pinHttpsCertificate();

            final int timeout = mArgs.getInt(InternalWebLoader.KEY_TIMEOUT);
            connection.setReadTimeout(timeout < 0 ? 0 : timeout);

            return connection; // Success
        }
        catch (Exception e)
        {
            Log.e("com.intel.commons.InternalWebLoader", "sendRequest", e);
            return null; // Error
        }
    }

    /**
     * Configures optional HTTP/S authentication.
     * <p>
     * Warning: this is a static setting for all transactions, meaning you can't use different
     * ones concurrently!
     */
    private void configureAuthentication()
    {
        final String username = mArgs.getString(InternalWebLoader.KEY_USERNAME);
        final String password = mArgs.getString(InternalWebLoader.KEY_PASSWORD);
        mAuthRetries = InternalWebLoader.AUTH_RETRIES;

        Authenticator.setDefault(new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                if (username != null && password != null)
                {
                    return (mAuthRetries-- <= 0) ?
                            null : new PasswordAuthentication(username, password.toCharArray());
                }
                else
                {
                    return null;
                }
            }
        });
    }

    /**
     * Configures optional pinning of the server's certificate (HTTPS only).
     * <p>
     * Warning: this is a static setting for all transactions, meaning you can't use different
     * ones concurrently!
     */
    private void pinHttpsCertificate()
    {
        final String issuer = mArgs.getString(InternalWebLoader.KEY_ISSUER);
        final String commonName = mArgs.getString(InternalWebLoader.KEY_COMMON_NAME);

        if (commonName != null && issuer != null)
        {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
            {
                @Override
                public boolean verify(String hostname, SSLSession session)
                {
                    try
                    {
                        final X509Certificate certificate = session.getPeerCertificateChain()[0];
                        final String issuerName = certificate.getIssuerDN().getName();
                        final String subjectName = certificate.getSubjectDN().getName();

                        return (issuerName.contains(issuer) && subjectName.contains(commonName));
                    }
                    catch(Exception e)
                    {
                        return false;
                    }
                }
            });
        }
    }

    /**
     * Receives the server's response for the given HTTP/S request.
     * <p>
     * Requires the permission {@link android.Manifest.permission.INTERNET}.
     *
     * @return {@code null} in case of low-level errors (HTTP error codes are handled gracefully)
     *
     * @see http://android-developers.blogspot.co.il/2011/09/androids-http-clients.html
     */
    private HttpResponse receiveResponse(HttpURLConnection connection)
    {
        // TODO: support binary downloads as well
        final StringBuilder response = new StringBuilder();
        InputStreamReader in = null;
        try
        {
            int length;
            final char[] buffer = new char[InternalWebLoader.BUFFER_SIZE];
            if (connection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST)
            {
                in = new InputStreamReader(connection.getInputStream());
            }
            else
            {
                in = new InputStreamReader(connection.getErrorStream());
            }
            while ((length = in.read(buffer)) != -1)
            {
                response.append(buffer, 0, length);
            }
            // Success
            return new HttpResponse(connection.getResponseCode(), connection.getResponseMessage(),
                    response.toString());
        }
        catch (Exception e)
        {
            Log.e("com.intel.commons.InternalWebLoader", "receiveResponse", e);
            return null; // Error
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (Exception e)
                {
                    // Silent failover, do nothing
                }
            }
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }
}
