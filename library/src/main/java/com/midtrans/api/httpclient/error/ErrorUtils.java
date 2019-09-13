package com.midtrans.api.httpclient.error;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ErrorUtils {
    private static final Logger LOGGER = Logger.getLogger("MIDTRANS");

    private static ErrorMessage parseError(final Response<?> response) {
        JSONObject bodyObj;
        ArrayList<Object> errorMessages = new ArrayList<>();
        try {
            assert response.errorBody() != null;
            String errorBody = response.errorBody().string();
            if (errorBody != null) {
                try {
                    bodyObj = new JSONObject(errorBody);
                    if (bodyObj.has("error_messages")) {
                        JSONArray errors = bodyObj.getJSONArray("error_messages");
                        for (int i = 0; i < errors.length(); i++) {
                            errorMessages.add(errors.get(i));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                return new ErrorMessage.Builder()
                        .defaultError()
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ErrorMessage.Builder()
                .errorMessage(errorMessages)
                .build();
    }

    public void httpErrorHandle(int code, Response response) {
        ErrorMessage errorMessage = parseError(response);
        switch (code) {
            case 400:
                LOGGER.warning("400 Bad Request: There was a problem in the JSON you submitted " + errorMessage.getErrorMessages());
                break;
            case 401:
                LOGGER.warning("401 Unauthorized: " + errorMessage.getErrorMessages());
                return;
            case 404:
                LOGGER.info("404 Not Found " + errorMessage.getErrorMessages());
                break;
            case 500:
                LOGGER.warning("HTTP ERROR 500: Internal Server ERROR! " + errorMessage.getErrorMessages());
                break;
            default:
                LOGGER.warning(errorMessage.getErrorMessages().toString());
                break;
        }
    }
}