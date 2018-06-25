package com.lansent.cannan.api.cookie;

// Declare any non-default types here with import statements

import com.lansent.cannan.api.cookie.MyResponse;

interface IMyNetService {
    void addResponse(in MyResponse response);
    void status(in int status);
}
