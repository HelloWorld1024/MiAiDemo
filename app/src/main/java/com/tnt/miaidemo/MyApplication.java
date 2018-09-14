package com.tnt.miaidemo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.xiaomi.ai.NeedUpdateTokenCallback;
import com.xiaomi.ai.SpeechEngine;
import com.xiaomi.ai.mibrain.Mibrainsdk;
import com.xiaomi.ai.utils.DeviceUtils;

import java.lang.ref.PhantomReference;

public class MyApplication extends Application {


    //private static final String Garen_CLIENT_ID = "352123259048697856" ;

    private static final String MACHINEONE_CLIENT_ID="356844666039051264";

    private static final String DEMO_CLINET_ID="326766038739850240";


    private static final String TAG = "MyApplication";
    private static Context mContext ;
    private static SpeechEngine mMiAiEngine ;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"MyApplication onCreate ") ;
        mContext = getApplicationContext() ;
        if (mContext == null) {
            Log.i(TAG,"getApplicationContext is null") ;
            return ;
        }
        Mibrainsdk.setLogLevel(Mibrainsdk.MIBRAIN_DEBUG_LEVEL_DEBUG);

        mMiAiEngine = SpeechEngine.createEngine(mContext,SpeechEngine.ENGINE_MI_AI,MACHINEONE_CLIENT_ID,true) ;
        mMiAiEngine.setEnv(SpeechEngine.ENV_PREVIEW);
        mMiAiEngine.updateTPAuth(Constants.MachineOne_token);
        mMiAiEngine.setUpdateTokenCallback(new UpdateToken());
        if (null == mMiAiEngine) {
            Log.i(TAG,"mMiAiEngine created failed") ;
        }else {
            Log.i(TAG,"mMiAiEngine created Success") ;
        }

        String device_id = DeviceUtils.getDeviceId(mContext) ;

        String authorizationValue =mMiAiEngine.getAuthorizationValue() ;

        Log.i(TAG,"deviceId = "+device_id+" AuthorizationValue="+authorizationValue) ;

    }



    public static SpeechEngine getMiAiEngine() {
        if (null == mMiAiEngine) {
            Log.i(TAG,"create new engine") ;
            mMiAiEngine = SpeechEngine.createEngine(mContext,SpeechEngine.ENGINE_MI_AI,MACHINEONE_CLIENT_ID,true) ;
        }
        return mMiAiEngine ;
    }

    class UpdateToken implements NeedUpdateTokenCallback{

        @Override
        public String onNeedUpdateToken() {
            Log.i(TAG,"need update token") ;
            return Constants.MachinOne_refresh_token;
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG,"mMiAiEngine onTerminate") ;
        if (null!= mMiAiEngine) {
            mMiAiEngine.release();
        }
    }


}
