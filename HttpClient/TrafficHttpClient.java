package cn.com.egova.securities_police.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.com.egova.securities_police.SecurityApplication;
import cn.com.egova.securities_police.model.Download.AppVersion;
import cn.com.egova.securities_police.model.accident.Accident;
import cn.com.egova.securities_police.model.accident.AccidentInfoFromQuery;
import cn.com.egova.securities_police.model.accident.AccidentInfoQueryReply;
import cn.com.egova.securities_police.model.accident.AccidentProof;
import cn.com.egova.securities_police.model.accident.InsuranceClaimInfoFromQuery;
import cn.com.egova.securities_police.model.accident.Litigant;
import cn.com.egova.securities_police.model.accident.LitigantsInfos;
import cn.com.egova.securities_police.model.accident.ProofTemplate;
import cn.com.egova.securities_police.model.accident.errors.AccidentDealResultReply;
import cn.com.egova.securities_police.model.bug.BugSack;
import cn.com.egova.securities_police.model.entity.DataSource_Diff;
import cn.com.egova.securities_police.model.entity.Dictionaries;
import cn.com.egova.securities_police.model.entity.HomeAddons;
import cn.com.egova.securities_police.model.entity.HttpReply;
import cn.com.egova.securities_police.model.entity.InsuranceCompany;
import cn.com.egova.securities_police.model.entity.PlateType;
import cn.com.egova.securities_police.model.entity.ThirdLoginReply;
import cn.com.egova.securities_police.model.entity.TrafficNews;
import cn.com.egova.securities_police.model.entity.User;
import cn.com.egova.securities_police.model.entity.UserDriverLicense;
import cn.com.egova.securities_police.model.entity.UserInfo;
import cn.com.egova.securities_police.model.entity.UserVehicle;
import cn.com.egova.securities_police.model.entity.VehicleShareInfo;
import cn.com.egova.securities_police.model.entity.Violation;
import cn.com.egova.securities_police.model.http.HttpRequstConstant;
import cn.com.egova.securities_police.model.requestBO.AccidentQueryBO;
import cn.com.egova.securities_police.model.requestBO.TrafficNewsQueryBO;
import cn.com.egova.securities_police.model.util.LogUtil;
import cn.com.egova.securities_police.ui.accidentReport.ResponsibilityResult1Fragment;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 结合RxAndroid以及Retrofit的HttpClient
 * Created by ZhangHaoSong on 2016/11/17 0017.
 */
public class TrafficHttpClient {
    private static TrafficHttpClient instance;

    public static TrafficHttpClient getInstance() {
        if (instance == null) {
            synchronized (TrafficHttpClient.class) {
                if (instance == null) {
                    instance = new TrafficHttpClient();
                }
            }
        }
        return instance;
    }

    private TrafficHttpClient() {
    }


    /**
     * 请求超时时长
     */
    private static final int DEFAULT_TIMEOUT = 5;
    /**
     * OkHttpClient用来添加固定的请求HEAD
     */
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder().connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

    /**
     * 多格式请求
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    /**
     * 添加BaseUrl以及对Gson和Rxjava的依赖,每次创建
     */
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(TrafficHttpContrains.API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create());

    /**
     * 正常的包括AccessToken请求头的请求,Json
     *
     * @param serviceClass
     * @param accessToken
     * @param <S>
     * @return
     */
    private <S> S createService(Class<S> serviceClass, final String accessToken) {
        if (accessToken != null) {
            //添加固定的请求Header
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();
                    // Request customization: add request headers
                    Request.Builder requestBuilder = original.newBuilder()
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_AUTHORIZATION_KEY, HttpRequstConstant.STANDARD_HTTP_HEAD_AUTHORIZATION_VALUE + accessToken)
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_CONTENT_TYPE_KEY, HttpRequstConstant.STANDARD_HTTP_HEAD_CONTENT_TYPE_VALUE)
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_ACCESS_TOKEN_KEY, accessToken)
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_DEVICE_TOKEN_KEY, SecurityApplication.device_token)
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_DEVICE_TYPE_KEY, SecurityApplication.device_type)
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_VERSION_KEY, SecurityApplication.VERSION)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }
        //创建Retrofit客户端
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }


    /**
     * 正常的包括AccessToken请求头的请求,multipart内容类型
     *
     * @param serviceClass
     * @param accessToken
     * @param <S>
     * @return
     */
    private <S> S createFileService(Class<S> serviceClass, final String accessToken) {
        if (accessToken != null) {
            //添加固定的请求Header
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();
                    // Request customization: add request headers
                    Request.Builder requestBuilder = original.newBuilder()
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_AUTHORIZATION_KEY, HttpRequstConstant.STANDARD_HTTP_HEAD_AUTHORIZATION_VALUE + accessToken)
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_CONTENT_TYPE_KEY, MULTIPART_FORM_DATA)
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_ACCESS_TOKEN_KEY, accessToken)
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_DEVICE_TOKEN_KEY, SecurityApplication.device_token)
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_DEVICE_TYPE_KEY, SecurityApplication.device_type)
                            .header(HttpRequstConstant.STANDARD_HTTP_HEAD_VERSION_KEY, SecurityApplication.VERSION)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }
        //创建Retrofit客户端
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    /**
     * 正常的不包括AccessToken请求头的请求,multipart内容类型
     *
     * @param serviceClass
     * @param <S>
     * @return
     */
    private <S> S createFileService(Class<S> serviceClass) {
        //添加固定的请求Header
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .header(HttpRequstConstant.STANDARD_HTTP_HEAD_CONTENT_TYPE_KEY, MULTIPART_FORM_DATA)
                        .header(HttpRequstConstant.STANDARD_HTTP_HEAD_DEVICE_TOKEN_KEY, SecurityApplication.device_token)
                        .header(HttpRequstConstant.STANDARD_HTTP_HEAD_DEVICE_TYPE_KEY, SecurityApplication.device_type)
                        .header(HttpRequstConstant.STANDARD_HTTP_HEAD_VERSION_KEY, SecurityApplication.VERSION)
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        //创建Retrofit客户端
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    /**
     * 不包含请求头的普通操作
     *
     * @param serviceClass
     * @param <S>
     * @return
     */
    private <S> S createService(Class<S> serviceClass) {
        //创建Retrofit客户端
        //添加固定的请求Header
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .header(HttpRequstConstant.STANDARD_HTTP_HEAD_DEVICE_TOKEN_KEY, SecurityApplication.device_token)
                        .header(HttpRequstConstant.STANDARD_HTTP_HEAD_DEVICE_TYPE_KEY, SecurityApplication.device_type)
                        .header(HttpRequstConstant.STANDARD_HTTP_HEAD_VERSION_KEY, SecurityApplication.VERSION)
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    /**
     * 获取法律条文
     *
     * @param subscriber
     * @param accessToken
     */
    public void getTrafficLaw(Subscriber<HttpReply<ArrayList<Dictionaries>>> subscriber, String accessToken) {
        createService(TrafficHttpContrains.TrafficLawService.class, accessToken).getTrafficLaw()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取法律条文
     *
     * @param accessToken
     */
    public void getTrafficLaw(TrafficHttpResponse response, String accessToken) {
        TrafficSubscriber subscriber = new TrafficSubscriber("zz", response);
        createService(TrafficHttpContrains.TrafficLawService.class, accessToken).getTrafficLaw()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber.getSubscriber());
    }

    /**
     * 注册新用户
     *
     * @param subscriber
     * @param user
     */
    public void register(Subscriber<HttpReply<Boolean>> subscriber, User user) {
        //默认给请求头添加了contentType json
        createService(TrafficHttpContrains.RegisterService.class).register(user)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 随手拍上传
     *
     * @param subscriber
     * @param accessToken
     * @param replied
     * @param bmpA
     * @param bmpB
     * @param bmpC
     * @param description
     * @param position
     * @param roadName
     * @param regionCode
     * @param regionName
     * @param longitude
     * @param latitude
     */
    public void upLoadIllegal(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, boolean replied, File bmpA, File bmpB, File bmpC, String description, String position, String roadName, String regionCode, String regionName, String longitude, String latitude) {
        //添加FileMap
        List<MultipartBody.Part> list = new ArrayList<>();
        //添加图片证据
        if (null != bmpA && bmpA.exists()) {
            LogUtil.e("zzz", "proofA input");
            MultipartBody.Part bmpAb = MultipartBody.Part.createFormData("files", "proofA.bmp", RequestBody.create(MediaType.parse("image/jpeg"), bmpA));
            list.add(bmpAb);
        }
        //添加图片证据
        if (null != bmpB && bmpB.exists()) {
            LogUtil.e("zzz", "proofB input");
            MultipartBody.Part bmpBb = MultipartBody.Part.createFormData("files", "proofB.bmp", RequestBody.create(MediaType.parse("image/jpeg"), bmpB));
            list.add(bmpBb);
        }
        //添加图片证据
        if (null != bmpC && bmpC.exists()) {
            LogUtil.e("zzz", "proofC input");
            MultipartBody.Part bmpCb = MultipartBody.Part.createFormData("files", "proofC.bmp", RequestBody.create(MediaType.parse("image/jpeg"), bmpC));
            list.add(bmpCb);
        }

        //添加其他的参数Body
        RequestBody descriptionRb = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody positionRb = RequestBody.create(MediaType.parse("text/plain"), position);
        RequestBody roadNameRb = RequestBody.create(MediaType.parse("text/plain"), roadName);
        RequestBody regionCodeRb = RequestBody.create(MediaType.parse("text/plain"), regionCode);
        RequestBody regionNameRb = RequestBody.create(MediaType.parse("text/plain"), regionName);
        RequestBody longitudeRb = RequestBody.create(MediaType.parse("text/plain"), longitude);
        RequestBody latitudeRb = RequestBody.create(MediaType.parse("text/plain"), latitude);
        RequestBody repliedRb = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(replied));
        //添加FileMap
        HashMap<String, RequestBody> map = new HashMap<>();

        map.put("description", descriptionRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_POSITION, positionRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_ROAD_NAME, roadNameRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_REGION_CODE, regionCodeRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_REGION_NAME, regionNameRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_LONGITUDE, longitudeRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_LATITUDE, latitudeRb);
        map.put(HttpRequstConstant.UPLOAD_VIOLATION_VALUE_REPLIED, repliedRb);

        //默认给请求头添加了contentType json
        createFileService(TrafficHttpContrains.UploadIllegalService.class, accessToken).uploadIllegal(list, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 证据上传
     */
    public void upLoadProofs(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, String accidentId, HashMap<Integer, String> photoMap, List<ProofTemplate> proofTemplateList, File recorderFile, String accidentType, String timestamp, String vehicleCount, String position, String roadName, String regionCode, String regionName, String longitude, String latitude, String weather, String accidentSource) {
        //添加FileMap
        List<MultipartBody.Part> list = new ArrayList<>();
        //添加图片证据
        for (Map.Entry<Integer, String> entry : photoMap.entrySet()) {
            LogUtil.e("zzz", "entry.getKey() =" + entry.getKey());
            LogUtil.e("zzz", "proofTemplateList.get(entry.getKey()).getName() =" + proofTemplateList.get(entry.getKey()).getName());
            MultipartBody.Part proof = MultipartBody.Part.createFormData("file", proofTemplateList.get(entry.getKey()).getName() + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), new File(entry.getValue())));
            list.add(proof);
        }
        //添加录音证据
        if (null != recorderFile && recorderFile.exists()) {
            MultipartBody.Part record = MultipartBody.Part.createFormData("file", AccidentProof.ProofTypeEn[AccidentProof.ProofTypeEn.length - 1] + ".amr", RequestBody.create(MediaType.parse("application/octet-stream"), recorderFile));
            list.add(record);
            LogUtil.e("zzz", "recorderFile name =" + AccidentProof.ProofTypeEn[AccidentProof.ProofTypeEn.length - 1] + ".amr");
        }


        //添加其他的参数Body
        RequestBody accidentTypeRb = RequestBody.create(MediaType.parse("text/plain"), accidentType);
        RequestBody timeStampRb = RequestBody.create(MediaType.parse("text/plain"), timestamp);
        RequestBody vehicleCountRb = RequestBody.create(MediaType.parse("text/plain"), vehicleCount);
        RequestBody positionRb = RequestBody.create(MediaType.parse("text/plain"), position);
        RequestBody roadNameRb = RequestBody.create(MediaType.parse("text/plain"), roadName);
        RequestBody regionCodeRb = RequestBody.create(MediaType.parse("text/plain"), regionCode);
        RequestBody regionNameRb = RequestBody.create(MediaType.parse("text/plain"), regionName);
        RequestBody longitudeRb = RequestBody.create(MediaType.parse("text/plain"), longitude);
        RequestBody latitudeRb = RequestBody.create(MediaType.parse("text/plain"), latitude);
        RequestBody weatherRb = RequestBody.create(MediaType.parse("text/plain"), weather);
        RequestBody accidentSourceRb = RequestBody.create(MediaType.parse("text/plain"), accidentSource);
        RequestBody accidentIdRb = RequestBody.create(MediaType.parse("text/plain"), accidentId);

        //添加FileMap
        HashMap<String, RequestBody> map = new HashMap<>();
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_ACCIDENT_TYPE, accidentTypeRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_TIME_STAMP, timeStampRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_COUNT, vehicleCountRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_POSITION, positionRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_ROAD_NAME, roadNameRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_REGION_CODE, regionCodeRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_REGION_NAME, regionNameRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_LONGITUDE, longitudeRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_LATITUDE, latitudeRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_WEATHER, weatherRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_VEHICLE_ACCIDENT_SOURCE, accidentSourceRb);
        map.put(HttpRequstConstant.UPLOAD_PROOF_PARA_ID, accidentIdRb);

        //默认给请求头添加了contentType json
        createFileService(TrafficHttpContrains.UploadProofsService.class, accessToken).uploadProofs(list, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 上传远程定责错误信息
     */
    public void upLoadErroeInfo(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, HashMap<String, String> mErrorBmpMap, Accident accident, ArrayList<Litigant> mLitigants) {
        //添加FileMap
        List<MultipartBody.Part> list = new ArrayList<>();
        //添加图片证据
        for (Map.Entry<String, String> entry : mErrorBmpMap.entrySet()) {
            LogUtil.e("zzz", "entry.getKey() =" + entry.getKey());
            LogUtil.e("zzz", "proofTemplateList.get(entry.getKey()).getName() =" + entry.getKey() + ".jpg");
            MultipartBody.Part proof = MultipartBody.Part.createFormData("file", entry.getKey() + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), new File(entry.getValue())));
            list.add(proof);
        }

        HashMap<String, RequestBody> map = new HashMap<>();
        //添加Accident属性参数
        for (Map.Entry<String, String> entry : Accident.getAttributesMap(accident).entrySet()) {
            map.put("accident." + entry.getKey(), RequestBody.create(MediaType.parse("text/plain"), entry.getValue()));
        }
        //添加当事人属性参数
        //添加Litigants
        for (int i = 0; i < mLitigants.size(); i++) {
            for (Map.Entry<String, String> entry : Litigant.getAttributesMap(mLitigants.get(i)).entrySet()) {
                map.put("litigants[" + i + "]." + entry.getKey(), RequestBody.create(MediaType.parse("text/plain"), entry.getValue()));
            }
        }
        //默认给请求头添加了contentType json
        createFileService(TrafficHttpContrains.UploadErrorInfoService.class, accessToken).uploadErrorInfo(list, map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 上传事故受理错误信息
     */
    public void upLoadProofErroe(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, HashMap<String, String> mErrorBmpMap, String accidentId) {
        //添加FileMap
        List<MultipartBody.Part> list = new ArrayList<>();
        //添加图片证据
        for (Map.Entry<String, String> entry : mErrorBmpMap.entrySet()) {
            LogUtil.e("zzz", "entry.getKey() =" + entry.getKey());
            LogUtil.e("zzz", "proofTemplateList.get(entry.getKey()).getName() =" + entry.getKey() + ".jpg");
            MultipartBody.Part proof = MultipartBody.Part.createFormData("file", entry.getKey() + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), new File(entry.getValue())));
            list.add(proof);
        }

        //默认给请求头添加了contentType json
        createFileService(TrafficHttpContrains.UploadProofErrorService.class, accessToken).uploadProofInfo(list, accidentId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 上传用户头像
     *
     * @param subscriber
     * @param accessToken
     * @param avatar
     * @param userId
     */
    public void uploadAvatar(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, File avatar, String userId) {
        MultipartBody.Part avatarP = MultipartBody.Part.createFormData("file", "avatar.jpg", RequestBody.create(MediaType.parse("image/jpeg"), avatar));
        createFileService(TrafficHttpContrains.UploadUserAvatarService.class, accessToken).upload(userId, avatarP)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 下载文件
     *
     * @param subscriber
     * @param url
     */
    public void downLoadFile(Subscriber<ResponseBody> subscriber, String url) {
        createService(TrafficHttpContrains.DownLoadFileService.class).downLoad(url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 查询交通通知
     *
     * @param subscriber
     * @param accessToken
     * @param pageInfo
     */
    public void queryNews(Subscriber<HttpReply<ArrayList<TrafficNews>>> subscriber, String accessToken, TrafficNewsQueryBO pageInfo) {
        createService(TrafficHttpContrains.QueryNewsService.class, accessToken).queryNews(pageInfo)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 查询事故信息
     *
     * @param subscriber
     * @param accessToken
     * @param pageInfo
     */
    public void queryAccident(Subscriber<HttpReply<ArrayList<AccidentInfoFromQuery>>> subscriber, String accessToken, AccidentQueryBO pageInfo) {
        createService(TrafficHttpContrains.QueryAccidentService.class, accessToken).queryAccident(pageInfo)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 查询违法上报
     *
     * @param subscriber
     * @param accessToken
     * @param pageInfo
     */
    public void queryViolation(Subscriber<HttpReply<ArrayList<Violation>>> subscriber, String accessToken, AccidentQueryBO pageInfo) {
        createService(TrafficHttpContrains.QueryViolationService.class, accessToken).queryViolation(pageInfo)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 添加用户车辆
     *
     * @param subscriber
     * @param accessToken
     * @param vehicle
     */
    public void insertVehicle(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, UserVehicle
            vehicle) {
        createService(TrafficHttpContrains.InsertVehicleService.class, accessToken).insertVehicle(vehicle)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 更新用户车辆
     *
     * @param subscriber
     * @param accessToken
     * @param vehicle
     */
    public void updateVehicle(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, UserVehicle
            vehicle) {
        createService(TrafficHttpContrains.UpdateVehicleService.class, accessToken).updateVehicle(vehicle)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 移除用户车辆
     *
     * @param subscriber
     * @param accessToken
     * @param vehicleId
     */
    public void removeVehicle(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, String vehicleId) {
        createService(TrafficHttpContrains.removeVehicleService.class, accessToken).removeVehicle(vehicleId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 更改驾照信息
     *
     * @param subscriber
     * @param accessToken
     * @param license
     */
    public void updateLicense(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, UserDriverLicense license) {
        createService(TrafficHttpContrains.UpdateLicenseService.class, accessToken).updateLicense(license)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 更改用户信息
     *
     * @param subscriber
     * @param accessToken
     * @param userInfo
     */
    public void updateUserInfo(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, UserInfo userInfo) {
        createService(TrafficHttpContrains.UpdateUserInfoService.class, accessToken).updateUserInfo(userInfo)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 设置默认车辆
     *
     * @param subscriber
     * @param accessToken
     * @param vehicleId
     */
    public void setVehiclePerfercened(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, String vehicleId) {
        createService(TrafficHttpContrains.SetUserVehiclePerferencedService.class, accessToken).setVehiclePerferenced(vehicleId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取用户积分信息
     *
     * @param subscriber
     * @param accessToken
     * @param plateNo
     * @param plateType
     */
    public void getUserSocreInfo(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, String plateNo, String plateType) {
        createService(TrafficHttpContrains.GetUserScoreInfoService.class, accessToken).getUserScoreInfo(plateNo, String.valueOf(PlateType.parsePlateType2Index(plateType)))
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取保险公司
     */
    public void getInsurances(Subscriber<HttpReply<ArrayList<InsuranceCompany>>> subscriber) {
        createService(TrafficHttpContrains.GetInsurancesService.class).getInsurances()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取用户的信息
     *
     * @param subscriber
     * @param accessToken
     */
    public void getUser(Subscriber<User> subscriber, String accessToken) {
        createService(TrafficHttpContrains.GetUserService.class, accessToken).getUser()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取事故类型
     *
     * @param subscriber
     * @param accessToken
     */
    public void getAccidentTypes(Subscriber<HttpReply<ArrayList<Dictionaries>>> subscriber, String accessToken) {
        createService(TrafficHttpContrains.GetByKindService.class, accessToken).getAccidentTypes()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取所有事故模板
     *
     * @param subscriber
     * @param accessToken
     */
    public void getProofTemplates(Subscriber<HttpReply<ArrayList<ProofTemplate>>> subscriber, String accessToken) {
        createService(TrafficHttpContrains.GetProogTemplatesService.class, accessToken).getPlates()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * Json提交方式的AccidentInfo
     *
     * @param subscriber
     * @param accessToken
     * @param accident
     */
    public void postAccidentInfo(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, AccidentInfoFromQuery accident) {
        createService(TrafficHttpContrains.PostAccidentInfoService.class, accessToken).postAccidentInfo(accident)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 通过Id查询事故信息
     *
     * @param subscriber
     * @param accessToken
     * @param accidentId
     */
    public void getAccidentInfoById(Subscriber<AccidentInfoQueryReply> subscriber, String accessToken, String accidentId) {
        createService(TrafficHttpContrains.GetAccidentInfoService.class, accessToken).getAccident(accidentId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 保险报案
     *
     * @param subscriber
     * @param accessToken
     * @param accidentId
     */
    public void reportById(Subscriber<ResponsibilityResult1Fragment.ReportReply> subscriber, String accessToken, String accidentId) {
        createService(TrafficHttpContrains.ReportInsuranceService.class, accessToken).reportAccidentById(accidentId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 查询定责结果
     *
     * @param subscriber
     * @param accessToken
     * @param accidentId
     */
    public void getIdentifyResultById(Subscriber<AccidentDealResultReply> subscriber, String accessToken, String accidentId) {
        createService(TrafficHttpContrains.GetIdentifyResultService.class, accessToken).getIdentifyResult(accidentId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 查询受理结果
     *
     * @param subscriber
     * @param accessToken
     * @param accidentId
     */
    public void getProofIdentifyResultById(Subscriber<AccidentDealResultReply> subscriber, String accessToken, String accidentId) {
        createService(TrafficHttpContrains.GetProofsIdentifyResultService.class, accessToken).getProofsIdentifyResult(accidentId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 查询保险报案信息
     *
     * @param subscriber
     * @param accessToken
     */
    public void queryInsuranceReportByPage(Subscriber<HttpReply<ArrayList<InsuranceClaimInfoFromQuery>>> subscriber, String accessToken, AccidentQueryBO query) {
        createService(TrafficHttpContrains.QueryInsuranceReportService.class, accessToken).queryInsuranceReport(query)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 查询车辆分享信息
     *
     * @param subscriber
     * @param accessToken
     */
    public void getVehicleShareInfo(Subscriber<HttpReply<VehicleShareInfo>> subscriber, String accessToken, String vehicleId) {
        createService(TrafficHttpContrains.GetVehicleShareInfoService.class, accessToken).getVehicleShareInfo(vehicleId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 用户登出
     *
     * @param subscriber
     * @param accessToken
     */
    public void logoutUser(Subscriber<HttpReply<Boolean>> subscriber, String accessToken) {
        createService(TrafficHttpContrains.LogoutUserService.class, accessToken).logoutUser()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取短信验证码
     *
     * @param subscriber
     */
    public void getVerificationCode(Subscriber<HttpReply<Boolean>> subscriber, String phoneNum) {
        createService(TrafficHttpContrains.GetVerificationCodeService.class).getVerificationCode(phoneNum)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 验证短信验证码
     *
     * @param subscriber
     */
    public void checkVerificationCode(Subscriber<HttpReply<Boolean>> subscriber, String phoneNum, String verificationCode) {
        createService(TrafficHttpContrains.CheckVerificationCodeService.class).checkVerificationCode(phoneNum, verificationCode)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 重新设置密码
     *
     * @param subscriber
     */
    public void checkVerificationCode(Subscriber<HttpReply<Boolean>> subscriber, String userName, String password, String authcode) {
        createService(TrafficHttpContrains.ResetPasswordReportService.class).resetPassword(userName, password, authcode)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取外挂程序
     *
     * @param subscriber
     */
    public void getXtras(Subscriber<HttpReply<ArrayList<HomeAddons>>> subscriber, String accessToken) {
        createService(TrafficHttpContrains.GetXtrasService.class, accessToken).getXtras()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    /**
     * 上传单条Bug信息
     *
     * @param subscriber
     */
    public void reportBug(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, BugSack bug) {
        createService(TrafficHttpContrains.ReportBugService.class, accessToken).reportBug(bug)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取短信验证码（事故定责的时候）
     *
     * @param subscriber
     */
    public void getResponsibityComfirmVerificationCode(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, String userId, boolean isConsulted, String telNo) {
        if (isConsulted) {
            createService(TrafficHttpContrains.GetResponsibityComfirmVerificationCodeService.class, accessToken).getResponsibityComfirmVerificationCode(userId, "16015", telNo)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);
        } else {
            createService(TrafficHttpContrains.GetResponsibityComfirmVerificationCodeService.class, accessToken).getResponsibityComfirmVerificationCode(userId, "13016", telNo)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);
        }
    }

    /**
     * 补充事故信息的（验证码/签名）
     *
     * @param subscriber
     */
    public void confirmAccidentInfo(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, AccidentInfoFromQuery accidentInfo) {
        createService(TrafficHttpContrains.ConfirmAccidentInfoService.class, accessToken).confirmAccidentInfo(accidentInfo)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 用户是否存在
     *
     * @param subscriber
     */
    public void isUserExist(Subscriber<HttpReply<Boolean>> subscriber, String userName) {
        createService(TrafficHttpContrains.IsUserExistService.class).isUserExist(userName)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 切换用户名（绑定的手机号）
     *
     * @param subscriber
     * @param accessToken
     * @param userId
     * @param userName
     * @param authCode
     */
    public void changeUserName(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, String userId, String userName, String authCode) {
        createService(TrafficHttpContrains.ChangeUserNameService.class, accessToken).changeUserName(userId, userName, authCode)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 请求是否拥有最新版本
     *
     * @param subscriber
     */
    public void getLastestVersion(Subscriber<HttpReply<AppVersion>> subscriber) {
        createService(TrafficHttpContrains.GetLastestVersionService.class).getLastestVersion(DataSource_Diff.DEFAULT_DATASOURCE)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 第三方授权登陆
     *
     * @param subscriber
     */
    public void getAccessTokenFromThird(Subscriber<ThirdLoginReply> subscriber, String openId, String type, String accessToken, String userType) {
        createService(TrafficHttpContrains.GetAccessTokenFromThirdService.class).getAccessTokenFromThird(type, openId, accessToken, "mobile-client", "mobile", userType)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 检验当事人信息的合法性
     *
     * @param subscriber
     */
    public void litigantCheck(Subscriber<ResponseBody> subscriber, String accessToken, ArrayList<LitigantsInfos> litigantsInfos) {
        createService(TrafficHttpContrains.LitigantCheckService.class, accessToken).litigantCheck(litigantsInfos)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取车辆类型
     *
     * @param subscriber
     */
    public void getPlateType(Subscriber<ArrayList<PlateType.PlateTypeInfo>> subscriber) {
        createService(TrafficHttpContrains.GetPlateTypeService.class).getPlateType()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 用户对事故定责有争议时删除事故
     *
     * @param subscriber
     */
    public void abortAccidentById(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, String accidentId) {
        createService(TrafficHttpContrains.AbortAccidentByIdService.class, accessToken).abortAccidentById(accidentId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 聊天文件上传
     *
     * @param subscriber
     */
    public void uploadImFile(Subscriber<HttpReply<Boolean>> subscriber, File fileA) {
        MultipartBody.Part fileP = MultipartBody.Part.createFormData("file", fileA.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), fileA));
        createFileService(TrafficHttpContrains.UploadImFileService.class).uploadImFile(fileP)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 判断交警受理是否可用
     *
     * @param subscriber
     */
    public void getAppValidated(Subscriber<HttpReply<Boolean>> subscriber, String accessToken) {
        createService(TrafficHttpContrains.GetAppValidatedService.class, accessToken).getAppValidated()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 设置定责账户
     *
     * @param subscriber
     */
    public void setIdentifer(Subscriber<HttpReply<Boolean>> subscriber, String accessToken, String identifer, String password) {
        createService(TrafficHttpContrains.SetIdentiferService.class, accessToken).setIdentifer(identifer, password)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    /**
     * 存储retrofit下载的文件
     *
     * @param body
     * @param filePath
     * @return
     */
    public boolean writeResponseBodyToDisk(ResponseBody body, String filePath) {
        try {
            File futureStudioIconFile = new File(filePath);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    LogUtil.d("", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
