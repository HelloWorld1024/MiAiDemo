package com.tnt.miaidemo;

import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.xiaomi.ai.AsrListener;
import com.xiaomi.ai.AsrRequest;
import com.xiaomi.ai.Instruction;
import com.xiaomi.ai.InstructionListener;
import com.xiaomi.ai.NlpListener;
import com.xiaomi.ai.NlpRequest;
import com.xiaomi.ai.PCMInfo;
import com.xiaomi.ai.SpeechEngine;
import com.xiaomi.ai.SpeechError;
import com.xiaomi.ai.SpeechResult;
import com.xiaomi.ai.TtsListener;
import com.xiaomi.ai.TtsRequest;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "miai";


    private Button mBtnAsr ;
    private Button mBtnNlp ;
    private Button mBtnTts ;
    private ImageButton mBtnAll ;
    private TextView mTextResult ;


    AsrRequest mAsrRequest ;
    TtsRequest mTtsRequest ;
    NlpRequest mNlpRequest ;

    MyNlpListener mNlpListener ;
    MyAsrListener mAsrListener ;
    MyTtsListener  mTtsListener ;
    MyInstructionListener mInstructionListener ;
    FileOutputStream os = null ;

    JSONObject mGson  ;
    SpeechEngine mEngine ;

    MyHandler mHandler ;

    static class MyHandler extends Handler {
        private WeakReference<MainActivity> mActivityWeakReference ;

        public MyHandler(MainActivity activity){
            mActivityWeakReference = new WeakReference<>(activity) ;
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivityWeakReference.get() ;
            if (null == activity)return;
            activity.updateResult(msg) ;
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnAsr = findViewById(R.id.test_asr) ;
        mBtnNlp = findViewById(R.id.test_nlp) ;
        mBtnTts = findViewById(R.id.test_tts) ;
        mBtnAll = findViewById(R.id.test_all) ;
        mTextResult = findViewById(R.id.test_result) ;

        mEngine =MyApplication.getMiAiEngine() ;
        mHandler = new MyHandler(this) ;

        if (null == mEngine || null == mBtnAsr || null == mBtnNlp
                || null == mBtnTts || null == mBtnAll || null == mTextResult) {
        Log.i(TAG,"init egine failed")  ;
        return ;
        }
        initListener() ;

        MyClickListener listener = new MyClickListener() ;
        mBtnAsr.setOnClickListener(listener);
        mBtnNlp.setOnClickListener(listener);
        mBtnTts.setOnClickListener(listener);

        mBtnAll.setOnClickListener(listener);







    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume") ;
    }

    private void initListener() {
        mAsrListener = new MyAsrListener() ;
        mNlpListener = new MyNlpListener() ;
        mTtsListener = new MyTtsListener() ;
        mInstructionListener = new MyInstructionListener();

        mEngine.setAsrLisnter(mAsrListener);
        mEngine.setNlpListener(mNlpListener);
        mEngine.setTtsListener(mTtsListener);
        mEngine.setInstructionListener(mInstructionListener);

    }


    private void testAsr() {
        if (null == mAsrRequest) {
            mAsrRequest = new AsrRequest() ;
        }
        mAsrRequest.setUseVad(true);
        mAsrRequest.setDataInputMode(AsrRequest.DataInputMode.DATA_INPUT_MODE_RECORDER);
        mEngine.listenSpeech(mAsrRequest);
    }
    private void testTts() {
        //tts
        if (null == mTtsRequest) {
            mTtsRequest = new TtsRequest() ;
        }
        mTtsRequest.setTextToSpeak("今天天气很好");
        //ttsRequest.setTtsAsFileUrl(true);
        mEngine.setTTSMode(SpeechEngine.STREAMING_PLAYER_DEFAULT);
        mEngine.speak(mTtsRequest);
    }
    private void testNlp(){
        //nlp
        if (mNlpRequest == null) {
            mNlpRequest = new NlpRequest() ;
        }
        mNlpRequest.setLocationEnable(true);
        mNlpRequest.setContext("key","value");
        mNlpRequest.setTextToProcess("深圳今天天气怎么样");
        mEngine.semanticsParse(mNlpRequest);
    }

    public void testAll() {
        SpeechEngine.ParamBuilder paramBuilder = new SpeechEngine.ParamBuilder() ;
        if (null == mAsrRequest) {
            mAsrRequest = new AsrRequest() ;
            mAsrRequest.setUseVad(true);
            mAsrRequest.setDataInputMode(AsrRequest.DataInputMode.DATA_INPUT_MODE_RECORDER);
        }
        if (null == mNlpRequest) {
            mNlpRequest = new NlpRequest() ;
            mNlpRequest.setLocationEnable(true);
            mNlpRequest.setContext("key","value");
            mNlpRequest.setTextToProcess("深圳今天天气怎么样");
            mEngine.semanticsParse(mNlpRequest);
        }
        if (null == mTtsRequest) {
            mTtsRequest = new TtsRequest() ;
            mAsrRequest.setUseVad(true);
            mAsrRequest.setDataInputMode(AsrRequest.DataInputMode.DATA_INPUT_MODE_RECORDER);
        }
        paramBuilder.needAsr().setAsrRequest(mAsrRequest)
                    .needNlp().setNlpRequest(mNlpRequest)
                    .needTts().setTtsRequest(mTtsRequest);
        mEngine.startIntegrally(paramBuilder);


    }

    class MyTtsListener implements TtsListener{

        @Override
        public void onTtsTransStart() {
            Log.i(TAG,"MyTtsListener  onTtsTransStart") ;
        }

        @Override
        public void onTtsTransEnd(boolean b) {
            Log.i(TAG,"MyTtsListener  onTtsTransEnd"+b) ;
        }

        @Override
        public void onPlayStart(AudioTrack audioTrack) {
            Log.i(TAG,"MyTtsListener  onTtsTransEnd"+audioTrack.toString()+" channel = "+audioTrack.getChannelCount()) ;
            Message msg = Message.obtain() ;
            msg.obj = audioTrack ;
            mHandler.sendMessage(msg) ;
        }
        @Override
        public void onPlayFinish() {
            Log.i(TAG,"MyTtsListener  onTtsTransEnd") ;
        }
        @Override
        public void onError(SpeechError speechError) {
            Log.i(TAG,"MyTtsListener  onTtsTransEnd"+speechError.toString()) ;
        }

        @Override
        public void onPCMData(PCMInfo pcmInfo) {
            Log.i(TAG,"MyTtsListener  onTtsTransEnd"+pcmInfo.toString()) ;
        }

        @Override
        public void onTtsGotURL(String s) {
            Log.i(TAG,"MyTtsListener onTtsGotURL="+s) ;
        }
    }

    class MyNlpListener implements NlpListener{

        @Override
        public void onResult(SpeechResult speechResult) {
            Log.i(TAG,"MyNlpListener  onResult"+speechResult.getQuery()+"  "+speechResult.getAnswerText()) ;
            Message msg = Message.obtain() ;
            msg.what = Constants.UPDATE_NLP_RESULT ;
            msg.obj = speechResult ;
            mHandler.sendMessage(msg) ;


        }

        @Override
        public void onError(SpeechError speechError) {
            Log.i(TAG,"MyNlpListener  onError="+speechError.toString()) ;

        }
    }

    class MyAsrListener implements  AsrListener{

        @Override
        public void onReadyForSpeech() {
            Log.i(TAG,"MyAsrListener onReadyForSpeech") ;

        }

        @Override
        public void onBeginningOfSpeech() {
            Log.i(TAG,"MyAsrListener onBeginningOfSpeech") ;

        }

        @Override
        public void onRmsChanged(float v) {
            Log.i(TAG,"MyAsrListener onRmsChanged"+v) ;

        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            Log.i(TAG,"MyAsrListener onBufferReceived"+bytes.length) ;
            //witeData(null,bytes);

        }

        @Override
        public void onEndOfSpeech() {
            if (null != os){
                try{
                    os.close();
                }catch (IOException e){
                e.printStackTrace();
                }
            }

            Log.i(TAG,"MyAsrListener onEndOfSpeech") ;

        }

        @Override
        public void onError(SpeechError speechError) {
            Log.i(TAG,"MyAsrListener onError="+speechError.toString()) ;

        }
        @Override
        public void onResults(SpeechResult speechResult) {
            Log.i(TAG,"MyAsrListener onResults"+speechResult.getQuery()) ;
            Message msg = Message.obtain() ;
            msg.what = Constants.UPDATE_ASR_RESULT ;
            msg.obj = speechResult ;
            mHandler.sendMessage(msg) ;
        }
        @Override
        public void onPartialResults(SpeechResult speechResult) {
            Log.i(TAG,"MyAsrListener onPartialResults"+speechResult.getQuery()) ;
            Message msg = Message.obtain() ;
            msg.what = Constants.UPDATE_ASR_RESULT ;
            msg.obj = speechResult ;
            mHandler.sendMessage(msg) ;

        }
        @Override
        public void onEvent() {
            Log.i(TAG,"MyAsrListener onEvent") ;

        }
    }

    class MyInstructionListener implements InstructionListener {

        @Override
        public void onInstruction(Instruction[] instructions, SpeechResult speechResult) {
            Log.i(TAG,"onInstruction speechResult="+speechResult.getAnswerText()) ;
            for (Instruction instruction:instructions){
                Log.i(TAG,instruction.toString());
            }
        }
    }

    public void witeData(@Nullable String path,byte[] data){
        if (null == path)path="/sdcard/voicemiai.pcm";
        if (null ==data || data.length <=0)return ;

        try{
            os = new FileOutputStream(path);

        }catch (FileNotFoundException e ){
            Log.i(TAG,"e"+e.toString()) ;
        }
        try{
            os.write(data);
            os.flush();

        }catch (Exception e){

        }

    }
    private void updateResult(Message msg) {
        switch (msg.what){
            case Constants.UPDATE_ASR_PARTIAL_RESULT:
            case Constants.UPDATE_ASR_RESULT:
                if (msg.obj!= null && msg.obj instanceof SpeechResult) {
                    SpeechResult result = (SpeechResult) msg.obj ;
                    mTextResult.setText(result.getQuery());
                }

                break ;
            case Constants.UPDATE_NLP_RESULT:
                if (msg.obj!= null && msg.obj instanceof SpeechResult) {
                    SpeechResult result = (SpeechResult) msg.obj ;
                    mTextResult.setText(result.getAnswerText());
                }
                break ;
            case Constants.UPDATE_TTS_RESULT:
                if (null != msg.obj && msg.obj instanceof AudioTrack) {
                    AudioTrack audioTrack = (AudioTrack)msg.obj ;
                    audioTrack.play();

                }

                break ;
            default:
                break ;

        }

    }

    class MyClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.test_asr:
                    testAsr();
                    break;
                case R.id.test_nlp:
                    testNlp();
                    break ;
                case R.id.test_tts:
                    testTts();
                    break ;
                case R.id.test_all:
                    testAll() ;
                    break ;
                    default:
                        break ;
            }

        }
    }


}
