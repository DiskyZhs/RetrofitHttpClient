package cn.com.egova.securities_police.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.com.egova.securities_police.model.Download.AppVersion;
import cn.com.egova.securities_police.model.accident.AccidentInfoFromQuery;
import cn.com.egova.securities_police.model.accident.AccidentInfoQueryReply;
import cn.com.egova.securities_police.model.accident.InsuranceClaimInfoFromQuery;
import cn.com.egova.securities_police.model.accident.LitigantsInfos;
import cn.com.egova.securities_police.model.accident.ProofTemplate;
import cn.com.egova.securities_police.model.accident.errors.AccidentDealResultReply;
import cn.com.egova.securities_police.model.bug.BugSack;
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
import cn.com.egova.securities_police.model.requestBO.AccidentQueryBO;
import cn.com.egova.securities_police.model.requestBO.TrafficNewsQueryBO;
import cn.com.egova.securities_police.ui.accidentReport.ResponsibilityResult1Fragment;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

class TrafficHttpContrains {
    private static TrafficHttpContrains instance;

    public static TrafficHttpContrains getInstance() {
        if (instance == null) {
            synchronized (TrafficHttpContrains.class) {
                if (instance == null) {
                    instance = new TrafficHttpContrains();
                }
            }
        }
        return instance;
    }

    private TrafficHttpContrains() {
    }

    //接口参数
    static final String API_BASE_URL = "http://jljjapp.cn/accident/";
    //public static final String API_BASE_URL = "http://192.168.101.34:7788/";

    //get请求

    /**
     * 查询法律条文接口
     */
    interface TrafficLawService {
        @GET("dictitem/getItemListByKind.json?kind=TrafficLaw")
        Observable<HttpReply<ArrayList<Dictionaries>>> getTrafficLaw();
    }

    /**
     * 注册账户请求
     */
    interface RegisterService {
        @POST("appuser/register")
        Observable<HttpReply<Boolean>> register(@Body User user);
    }


    /**
     * 随手拍
     */
    interface UploadIllegalService {
        @Multipart
        @POST("m/violation/upload")
        Observable<HttpReply<Boolean>> uploadIllegal(@Part List<MultipartBody.Part> fileMap, @PartMap HashMap<String, RequestBody> map);
    }

    /**
     * 更换用户图像
     */
    interface UploadUserAvatarService {
        @Multipart
        @POST("m/appuser/upload/{userId}")
        Observable<HttpReply<Boolean>> upload(@Path("userId") String id, @Part MultipartBody.Part avatar);
    }

    /**
     * 下载文件
     */
    interface DownLoadFileService {
        @Streaming
        @GET
        Observable<ResponseBody> downLoad(@Url String fileUrl);
    }

    /**
     * 查询交通通知
     */
    interface QueryNewsService {
        @POST("m/news/page")
        Observable<HttpReply<ArrayList<TrafficNews>>> queryNews(@Body TrafficNewsQueryBO pageInfo);
    }

    /**
     * 查询事故信息
     */
    interface QueryAccidentService {
        @POST("m/accident/client_page")
        Observable<HttpReply<ArrayList<AccidentInfoFromQuery>>> queryAccident(@Body AccidentQueryBO pageInfo);
    }

    /**
     * 查询违法上报
     */
    interface QueryViolationService {
        @POST("m/violation/page")
        Observable<HttpReply<ArrayList<Violation>>> queryViolation(@Body AccidentQueryBO pageInfo);
    }

    /**
     * 添加用户车辆信息
     */
    interface InsertVehicleService {
        @POST("m/uservehicle/insert")
        Observable<HttpReply<Boolean>> insertVehicle(@Body UserVehicle vehicle);
    }

    /**
     * 更新车辆信息
     */
    interface UpdateVehicleService {
        @POST("m/uservehicle/update")
        Observable<HttpReply<Boolean>> updateVehicle(@Body UserVehicle vehicle);
    }

    /**
     * 移除车辆的信息
     */
    interface removeVehicleService {
        @GET("m/uservehicle/deleteById/{vehicleId}")
        Observable<HttpReply<Boolean>> removeVehicle(@Path("vehicleId") String vehicleId);
    }

    /**
     * 更新驾照信息
     */
    interface UpdateLicenseService {
        @POST("m/userdriverlicense/save")
        Observable<HttpReply<Boolean>> updateLicense(@Body UserDriverLicense license);
    }

    /**
     * 更新用户信息
     */
    interface UpdateUserInfoService {
        @POST("m/appuser/updat")
        Observable<HttpReply<Boolean>> updateUserInfo(@Body UserInfo userInfo);
    }

    /**
     * 设置默认车辆
     */
    interface SetUserVehiclePerferencedService {
        @GET("m/uservehicle/preferenced/{vehicleId}")
        Observable<HttpReply<Boolean>> setVehiclePerferenced(@Path("vehicleId") String vehicleId);
    }

    /**
     * 获取用户积分信息
     */
    interface GetUserScoreInfoService {
        @GET("m/traffic/inquiryhome/{plateNo}/{plateIndex}")
        Observable<HttpReply<Boolean>> getUserScoreInfo(@Path("plateNo") String plateNo, @Path("plateIndex") String plateIndex);
    }

    /**
     * 请求获取支持的保险公司的信息
     */
    interface GetInsurancesService {
        @GET("Accident/insurances")
        Observable<HttpReply<ArrayList<InsuranceCompany>>> getInsurances();
    }

    /**
     * 获取用户的信息
     */
    interface GetUserService {
        @GET("m/appuser/current")
        Observable<User> getUser();
    }

    /**
     * 获取事故类型
     */
    interface GetByKindService {
        @GET("dictitem/getItemListByKind.json?kind=AccidentType")
        Observable<HttpReply<ArrayList<Dictionaries>>> getAccidentTypes();
    }

    /**
     * 获取所有事故模板
     */
    interface GetProogTemplatesService {
        @GET("m/prooftemplate/all")
        Observable<HttpReply<ArrayList<ProofTemplate>>> getPlates();
    }

    /**
     * 上传证据
     */
    interface UploadProofsService {
        @Multipart
        @POST("m/accident/upload")
        Observable<HttpReply<Boolean>> uploadProofs(@Part List<MultipartBody.Part> fileMap, @PartMap HashMap<String, RequestBody> map);
    }

    /**
     * Json提交方式的AccidentInfo
     */
    interface PostAccidentInfoService {
        @POST("m/accident/assume")
        Observable<HttpReply<Boolean>> postAccidentInfo(@Body AccidentInfoFromQuery accident);
    }

    /**
     * 通过Id查询事故信息
     */
    interface GetAccidentInfoService {
        @GET("m/accident/client_get/{accidentId}")
        Observable<AccidentInfoQueryReply> getAccident(@Path("accidentId") String accidentId);
    }

    /**
     * 保险报案
     */
    interface ReportInsuranceService {
        @GET("m/accident/report/{accidentId}")
        Observable<ResponsibilityResult1Fragment.ReportReply> reportAccidentById(@Path("accidentId") String accidentId);
    }

    /**
     * 查询远程定责结果
     */
    interface GetIdentifyResultService {
        @GET("m/accident/getIdentifyResult/{accidentId}")
        Observable<AccidentDealResultReply> getIdentifyResult(@Path("accidentId") String accidentId);
    }


    /**
     * 上传远程定责错误信息
     */
    interface UploadErrorInfoService {
        @Multipart
        @POST("m/accident/assumeAll")
        Observable<HttpReply<Boolean>> uploadErrorInfo(@Part List<MultipartBody.Part> fileMap, @PartMap HashMap<String, RequestBody> map);
    }

    /**
     * 查询事故受理结果
     */
    interface GetProofsIdentifyResultService {
        @GET("m/accident/getAcceptResult/{accidentId}")
        Observable<AccidentDealResultReply> getProofsIdentifyResult(@Path("accidentId") String accidentId);
    }

    /**
     * 上传事故受理错误信息
     */
    interface UploadProofErrorService {
        @Multipart
        @POST("m/accident/updateProofs/{accidentId}")
        Observable<HttpReply<Boolean>> uploadProofInfo(@Part List<MultipartBody.Part> fileMap, @Path("accidentId") String accidentId);
    }

    /**
     * 分页查询保险报案信息
     */
    interface QueryInsuranceReportService {
        @POST("m/insuranceClaim/client/page")
        Observable<HttpReply<ArrayList<InsuranceClaimInfoFromQuery>>> queryInsuranceReport(@Body AccidentQueryBO query);
    }

    /**
     * 查询车辆分享信息
     */
    interface GetVehicleShareInfoService {
        @GET("m/uservehicle/getLitigant/{vehicleId}")
        Observable<HttpReply<VehicleShareInfo>> getVehicleShareInfo(@Path("vehicleId") String vehicleId);
    }

    /**
     * 用户登出
     */
    interface LogoutUserService {
        @GET("oauth2/revoke-token")
        Observable<HttpReply<Boolean>> logoutUser();
    }

    /**
     * 获取短信验证码
     */
    interface GetVerificationCodeService {
        @GET("message/send/{phone}")
        Observable<HttpReply<Boolean>> getVerificationCode(@Path("phoneNum") String phoneNum);
    }

    /**
     * 验证短信验证码
     */
    interface CheckVerificationCodeService {
        @GET("message/verif/{phoneNum}/{verificationCode}")
        Observable<HttpReply<Boolean>> checkVerificationCode(@Path("phone") String phone, @Path("verificationCode") String verificationCode);
    }

    /**
     * 重新设置密码
     */
    interface ResetPasswordReportService {
        @POST("appuser/change/password")
        Observable<HttpReply<Boolean>> resetPassword(@Field("username") String username, @Field("password") String password, @Field("authenticode") String authenticode);
    }

    /**
     * 获取外挂程序
     */
    interface GetXtrasService {
        @GET("m/xtras/all")
        Observable<HttpReply<ArrayList<HomeAddons>>> getXtras();
    }

    /**
     * 上传单条Bug信息
     */
    interface ReportBugService {
        @POST("m/bugsack/insert")
        Observable<HttpReply<Boolean>> reportBug(@Body BugSack bug);
    }

    /**
     * 获取短信验证码（事故定责的时候）
     */
    interface GetResponsibityComfirmVerificationCodeService {
        @GET("m/litigant/getVerificationCodeByPhone")
        Observable<HttpReply<Boolean>> getResponsibityComfirmVerificationCode(@Query("id") String userId, @Query("templateid") String templateid, @Query("phone") String phoneNo);
    }

    /**
     * 补充事故信息的（验证码/签名）
     */
    interface ConfirmAccidentInfoService {
        @POST("m/accident/confirm")
        Observable<HttpReply<Boolean>> confirmAccidentInfo(@Body AccidentInfoFromQuery accidentInfo);
    }

    /**
     * 用户是否存在
     */
    interface IsUserExistService {
        @GET("appuser/existsByName/{userName}")
        Observable<HttpReply<Boolean>> isUserExist(@Path("userName") String userName);
    }

    /**
     * 切换用户名（绑定的手机号）
     */
    interface ChangeUserNameService {
        @GET("m/appuser/changeUserName")
        Observable<HttpReply<Boolean>> changeUserName(@Query("id") String userId, @Query("username") String userName, @Query("code") String authCode);
    }

    /**
     * 请求是否拥有最新版本
     */
    interface GetLastestVersionService {
        @GET("home/getversion/{dataSource}")
        Observable<HttpReply<AppVersion>> getLastestVersion(@Path("dataSource") String dataSource);
    }

    /**
     * 第三方授权登陆
     */
    interface GetAccessTokenFromThirdService {
        @GET("oauth2/{type}/token")
        Observable<ThirdLoginReply> getAccessTokenFromThird(@Path("type") String type, @Query("openid") String openId, @Query("access_token") String accessToken, @Query("client_id") String clientId, @Query("client_secret") String clientSecret, @Query("userType") String userType);
    }

    /**
     * 检验当事人信息的合法性
     */
    interface LitigantCheckService {
        @POST("m/accident/validatePolicy")
        Observable<ResponseBody> litigantCheck(@Body ArrayList<LitigantsInfos> litigantsInfos);
    }

    /**
     * 获取车辆类型
     */
    interface GetPlateTypeService {
        @GET("dictitem/getByKind?kind=PlateType")
        Observable<ArrayList<PlateType.PlateTypeInfo>> getPlateType();
    }

    /**
     * 用户对事故定责有争议时删除事故
     */
    interface AbortAccidentByIdService {
        @GET("m/accident/discard")
        Observable<HttpReply<Boolean>> abortAccidentById(@Query("key") String accidentId);
    }

    /**
     * 聊天文件上传
     */
    interface UploadImFileService {
        @Multipart
        @POST("attachment/upload")
        Observable<HttpReply<Boolean>> uploadImFile(@Part MultipartBody.Part file);
    }

    /**
     * 判断交警受理是否可用
     */
    interface GetAppValidatedService {
        @GET("m/app/validate")
        Observable<HttpReply<Boolean>> getAppValidated();
    }

    /**
     * 设置定责账户
     */
    interface SetIdentiferService {
        @GET("m/appuser/authority/save")
        Observable<HttpReply<Boolean>> setIdentifer(@Query("indentifier") String identifer, @Query("password") String password);
    }
}
