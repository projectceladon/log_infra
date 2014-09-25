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

import java.net.HttpURLConnection;

/**
 * Simple container for HTTP response data, returned by {@link InternalWebLoader} to
 * {@link WebLoader} subclasses.
 */
public final class HttpResponse
{
    private int mCode;
    private String mMessage;
    private String mContent;

    HttpResponse(int code, String message, String content)
    {
        mCode = code;
        mMessage = message;
        mContent = content;
    }

    /**
     * The status code of the response obtained from the HTTP request -
     * see {@link HttpURLConnection} constants.
     * <p>
     * <ul>
     *   <li>1xx: Informational</li>
     *   <li>2xx: Success</li>
     *   <li>3xx: Relocation/Redirection</li>
     *   <li>4xx: Client Error</li>
     *   <li>5xx: Server Error</li>
     * </ul>
     */
    public int getCode()
    {
        return mCode;
    }

    /**
     * The HTTP response message which corresponds to {@link #getCode()}.
     */
    public String getMessage()
    {
        return mMessage;
    }

    /**
     * {@code null} in case of any error.
     *
     * TODO: Convert to byte[]
     */
    public String getContent()
    {
        return mContent;
    }
}
