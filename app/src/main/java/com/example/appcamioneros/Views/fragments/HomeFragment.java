package com.example.appcamioneros.Views.fragments;


import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.appcamioneros.R;
import com.example.appcamioneros.Services.ApiService;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.light.Position;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * A simple {@link Fragment} subclass. // MapboxMap.OnMapClickListener
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    private Retrofit retrofit;
    private ApiService service;
    private MapView mapView;
    private GeoJsonSource geoJsonSource;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private Button btnEmergencia;
    private Handler handler;
    private Runnable runnable;
    private LatLng currentPosition;
    private int count = 0;
    private ImageView imageAdd, imageRemove, imageGps;
    private Double Latitude;
    private Double Longitude;
    private Context context;
    private View layout;
    private SharedPreferences pref;
    private List<Point> points;
    private ValueAnimator markerIconAnimator;
    private LatLng markerIconCurrentLocation;
    private GeoJsonSource dotGeoJsonSource;
    private ValueAnimator animator;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        context = view.getContext();
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_home);
        pref = getContext().getSharedPreferences("user",getContext().MODE_PRIVATE);
        retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.Server))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ApiService.class);
        imageAdd = view.findViewById(R.id.iconAdd);
        imageRemove = view.findViewById(R.id.iconRemove);
        btnEmergencia = (Button) view.findViewById(R.id.btnEmergencia);
        imageGps = view.findViewById(R.id.imageGps);
        imageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onZoom(1);
            }
        });
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                HomeFragment.this.mapboxMap = mapboxMap;



                mapboxMap.addOnMoveListener(new MapboxMap.OnMoveListener() {
                    @Override
                    public void onMoveBegin(MoveGestureDetector detector) {

                    }

                    @Override
                    public void onMove(MoveGestureDetector detector) {
                    }
                    @Override
                    public void onMoveEnd(MoveGestureDetector detector) {
                    }
                });

                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        polyLine(style);
//                        mapboxMap.addOnMapClickListener(HomeFragment.this);
                    }
                });
            }
        });
        imageRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onZoom(-1);
            }
        });
        imageGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positionUser();
            }
        });
        btnEmergencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEmergencia();

            }
        });

        return view;
    }

    private void initRunnable() {
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                if(points.size() -1 >= count) {
                    LatLng latLng = new LatLng(points.get(count).latitude(), points.get(count).longitude());
                    if (animator != null && animator.isStarted()) {
                        currentPosition = (LatLng) animator.getAnimatedValue();
                        animator.cancel();
                    }
                    animator = ObjectAnimator
                            .ofObject(latLngEvaluator, currentPosition, latLng)
                            .setDuration(1000);
                    animator.addUpdateListener(animatorUpdateListener);
                    animator.start();

                    currentPosition = latLng;
                    positionUser();
                    count++;
                    handler.postDelayed(this, 1000);
                    if(count==points.size()){
                        count = 0;
                    }
                }
            }
        };
        handler.post(runnable);
    }


//    @Override
//    public boolean onMapClick(@NonNull LatLng point) {
////        if (animator != null && animator.isStarted()) {
////            currentPosition = (LatLng) animator.getAnimatedValue();
////            animator.cancel();
////        }
////
////        animator = ObjectAnimator
////                .ofObject(latLngEvaluator, currentPosition, point)
////                .setDuration(2000);
////        animator.addUpdateListener(animatorUpdateListener);
////        animator.start();
////        currentPosition = point;
////
////        return true;
//    }

    private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {

        private final LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    };

    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                    geoJsonSource.setGeoJson(Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude()));
                }
            };




    private void polyLine (@NonNull Style style) {
        String polyline = pref.getString("polyline", "");
        int danger = pref.getInt("danger",0);
        int colorLine = 0;
        if(danger==1) {
            colorLine = Color.RED;
        } else if(danger==2) {
            colorLine = Color.YELLOW;
        } else {
            colorLine = Color.GREEN;
        }
        if(!polyline.isEmpty()) {
            points = PolylineUtils.decode("['cm|]hbvkMj@Lt@RSp@St@hBdBGx@?bAWBwBh@Hr@@FFh@Lz@Hl@Fh@Hl@Hd@@FHApBQF?DBD@BDLZJP@LETGRs@nAAVkBhD{D~GUXQRYReCpAs@h@WRMH}@f@e@Zq@^k@X[NQHMDOFQDMDIDc@LOHEBA@QNMNKPIRELENYbBGPILKLIFIDGD{BnAOPMXCNCV@b@RrAH`@BRBJFPFRFNJRPZZf@^d@HJDDJNFJFNBNBP?PANAJGRGPINKLy@r@ILABEHELANATBtBA^@TRx@JZHP@DBFHFJ?JCFG\\\\KNCHCr@Al@Bn@D^JLTETYXkC|@ONEJCFENBVh@fANXFRBPAVWtAANBP@FJNPNXL^Fx@Bn@INCHAZGZET?\\\\DHBbAP~@VRNFRK`@KTITAH?HBJFLFJJLHJDJH`@DdCFbAB`AEL?FAF@LFLHRJ@LBL@VDNDZNhAbAl@j@n@n@RVNVFRHZDZ@TENYb@NvAFp@FhAHxAFnB?T?BBbB@N@N@dCB~B?zBHbCFfCFzBF~BFbCDbCF`CD|B@|BFbCJzBFdCF`CDfCBz@Bx@jBGvA?jBGjBCz@AfEInDGD?HLBVC`@M\\\\ILIHQHOJqAtB_@hAWx@a@jACJALAR?|@?P^@~A?P@V@h@B~BVNB\\\\FTHlCr@JFRJ`FpCJJLJJNRVzCvFP\\\\HNxAnCJNNTtBbCJJLHJFRFR@PCLC|FoB^QVOXSNORIVEx@?TCPIPQl@eALOPKROjGaCJCJAN?bAHv@B`@@P@V?R@R?N?P?P@R@RBRDZF^H^LXLPHPJNLPLLLPPHNHNHNBNDP@N@P?PAPAPENCRERCHCJERCZGb@CVCPELCJGLIHIHMJMFKDMDWDeATM@K@K?IAMCSIu@[GCICOEKEKAMAO?MBMBODMHKHKJIJGNGRIXGPGJIHIFKFMBM@M?_@Ce@CS@O@ODMFKFIHIHGLELCNAL@N@LBLDLFHLLJFJDJBJBJ@T?t@B~A@d@@T?PANANANCNELEPGJELILILKNKJGJCJCJ?J?H@LBJBHDFFFFBFDJ@H?L@nA@d@@LBJDJDHFHFFFFHFHDLFp@RXHPFJFDDDFDHDJBL?HAJCJCHGJGFGDKBIBK@K?KAKCKEKGIGKKKMGGGEKGKCKAEAG?K?I?K?G@KBMFKFKHGHELENEPGRGVGVETGREREPGPGPINIJCDMPORKPIJEHIHABIHKFIDKDOBMBS@g@By@Dc@D[BYDUFSFSHOHUJQLWRWTIHKLOPKNGNELENEZCPARAP?RBVBNFPFNLRJNNRVZ^d@NNFJFLDJBLBP@L?LALALCJELGLGLGJCLCNAL?L?NBLDLDHFHJHLLJHHJDHDNBN@N?JALCJCHGJGJOPoApAe@b@OLKLGJINOZMTMTQZOTIPEJCLAJ@L@LDJDJFFHHHFLHHFHFDHDJBJBN?L?JALCLCJIJOPSRMNOPOPMRIPGNGNCPEPAL?J?H?D@D@HBLBNJ\\\\Pl@@FNf@DPBJ@J?J?JAJCNEJEJEHKLKLMLIJGJGLCLCNEn@KnAG@C\\\\CNCLEJGJGLIFKHIDKBI?K?IAICMEIEIGMMWY_@@c@a@USKMIQGQEOCQAO?Q@Q@QDMBMFOJOLMPQTUf@@RQHGDGFGBKBI?K?KCICKCKEKGGGIIEIGMEMCOAM?O?S@]@S@M?QAOCQEOGUK_@SSKOEKCOAOAM@O@MBOFMFKHIHIJGJIPIRMZOf@K`@IZIPILIHKHKHKDQFOBQBKBMDKDKJIHIJGJGLGNELINILIJIHMHMFMBOB[D]DO?K?OAKCMGKIk@q@UW_AaAIKGKCKAM@ODa@@U@K?KAICKCKGKIMKIMG_A]eAc@KEIGIGIKGKEOEOASEUCSEOGOGMIMMKQOQQOOKKYWSQKIOIMEOCSAOAK?I@k@DI@GBKDIHGLGLEP?LALBLBTTlANr@@FFTDNBJ@H?J?JARARAPAJ?L@NBRFTFZJ\\\\J`@H\\\\DPBPBN@N@XB`@B`@Bf@@l@Bb@@T@PBNDLFNFLFLHLHLLJVRb@ZPLHHHJFJFLJb@Ld@BJDJDHDHFFHDZNVJLHHDHHHJHJFLDLFPJV~@zBb@hAJXHVBNBN?N?NANCXG`@ETCRAN?N?LBLDVDRDRBRBN@N@L?NALCRCPCTAPARA^AhBClB@X@P@NBNBLDLFLDJHHHHHFJFJFHBJBJBN@F@lBLl@D\\\\DNBLBJBJDJDLJNJTTPPJLNLNLPHRJRHLFVHdB^\\\\HNDPHRLPNNPJLJPLXTb@JRBHDLLXVz@Nh@HR@FHNHJJJLJNHZNZPPJPLVT`@^p@n@PPLPFNFNFPBNBVDrBBTBNHNFNJJNLNHLFNBN@J?J?LCJCHCJGLIPOJILIJELCJAJ@JBPH^VdAr@t@d@ZPVPNJHJHJDNBJ@L?JAJAJEHGHKJMFMFOHUHKFOJKJILGLKVUr@GRGLGNIHGHKHKFKHMDUHk@LaCf@m@Nc@HeALc@H[HSHUJULQJQLMNONMPINKRMXGLGJIJIHKHIFIDKFQDi@NQFIDIHIHGLELENAN?N@PBNFNFLLJVRl@^PLLLLLJNPXT`@TXLRNLNJNHRHPDPBN@L?J?LCJELENKRMJGJELCJAN?NBLD~At@jAj@NJJHHLFLDNJf@R|@FXFRHPHLJJLHn@\\\\jBz@XJNHNBPBP@N?LCLCLGRKNGNERCNAP?P@RBTFTJTN`Al@lBpAf@ZXPTNVLvAl@l@VVJVHZDd@F`@BTDPDPFLHPNLNJLHNDLFLBLBN@N?PAVGn@El@K`AIp@?N@LBPDNFNHJHHJFHFNDb@JdAPdB\\\\l@JNDLBNDLDPHTJ`@Rl@Zh@Zn@Zf@PZH\\\\DR@L@^BR@NBNBPFTHVJLDPFPDt@L|@NRDJDLBJDFBJFFFHHFFHLJRNTRd@b@x@NXPTNRPLLJRLVN^Rh@X~@f@\\\\RLFNDLDRBR@P@L@NAL?ZC|@KbAIb@EPANAL?N@N@NBPBPDNFXJPJl@b@`@Z\\\\Vn@d@XPLHJHNHPHTJPDPFPDXHz@TxA^ZHPFLDNHNHJFLJRPXZLNpArAbAfATRVPVNVJXHVHXDZFb@Dl@FjBPl@F`@FTDPDPFPHNHNHLHNLLLNPPXn@bADD~@nAh@v@RXDH|@nAf@t@j@@LPJNJNJJJJRJPHPHRD\\\\HjB\\\\@P^HPDPFNJLFLJNLPRV\\\\h@t@V\\\\LPLNPLLJNHRJTJXHhCt@bBd@bBd@|@T`@J`@L\\\\Lj@R`A\\\\ZLPHVNVPLH^VRLRLNFNFH@HBL?J@L?JANAd@GtAOf@G^ETCZCVAX?\\\\@R?XBZD\\\\Db@Hh@HTDPDPFRFZLTFTFTDr@JjARfAPtARx@Ln@HzAP`ALd@D^Fd@Fh@JVDj@Jz@Nr@HvCd@AVpDh@|Cb@rARj@JZF^HVJXN`@Tl@b@n@`@\\\\RTHPDRBP@R?T?REVEXGPGPKzA{@LGNILGLETGPEVCl@E`AE^Cz@ATAP@R@ZDZHp@Nj@Pf@N@ZRHPFVF\\\\FTBVB^@t@BR@J@J@RBPDPHLFJNrFlJXd@BDXh@R^Pd@HXDLbA|DF^DZ?b@ENKPGFGDIDEFEFEHAHAH@J@HDP@N@NARCXo@dB[jAO|@E`AA~@Bz@Hz@Lt@f@bB\\\\l@lCdFlA|Bb@z@Tx@F|@@n@Ad@Gp@O\\\\Uj@]v@Uj@Ob@K\\\\I^I^Eb@E`@Af@@^Bd@D\\\\Hb@H\\\\L\\\\LXT`@T\\\\VZXXVVXV^Xf@^tBzAz@l@l@`@t@`@XNXJZL\\\\J\\\\H`@H|AVn@JtAVpATJB\\\\FD@ZJXHTHVLVNVNXPXRx@n@pB~AnBvAfAz@`@\\\\z@l@b@\\\\PLHHRNXNZR`@Pf@TjAf@r@\\\\j@Vd@P\\\\NXJVHTFVH\\\\HTDTDRD\\\\DN@R@P@P?P?T?XAZCTCXEREPEVG`@Kt@SvA_@pA[j@M^Kd@KVEVGXE\\\\EZC\\\\C`@Cl@A`@A^Af@AlAC~AA`A?^@Z@ZBXDTBXF\\\\Hb@Np@V`CbAdDzAjAd@pAf@ZLf@NZHXFVDZDZDh@DpAFD?`@B|BLrAHl@Fz@H|ALpALj@DVBT@Z@^?\\\\?VAT?NAPARCVA~@MfAMn@Gp@Ix@M`AOTE^Gj@Kr@Mp@Ml@KHCbDu@h@KXIVGXITIXKVKRKTKBAVM\\\\ShBgAt@e@VOPITIVERCNATAV@R@PDNDNDRLp@b@dBrAv@f@JHNFLFPDPDP@N@P@L?NANANCPCZGv@Qp@OXIRGPETKPGXOTMROZU\\\\UTORKRINGNERCRCPAR?TBXDNBJDRJTLPLr@f@XPNHNHPFPFRBTBTBR?V?l@ApDE`CCdAA\\\\?V@^BhBPhBPxANTBJ@J@VDrAL~@JJ@p@HN@TDnAJdCTfCVfFh@^FjBRf@FXD\\\\Dd@Dv@FhBPJ@bBPbAJL@dEd@n@Hf@Dn@D|AJbBJh@DVBRBVDRDTFTHVJZNlAf@@`@tB|@tAj@|FhCtCtAfAf@nAj@nB@`Ab@j@Tj@T~DtAdA^h@Pr@Rd@LZHVDZFXDXBV@V@f@Bn@@P@|ADpCHrADhCJpAFl@Bd@@j@Bf@@h@@z@@^BV@J@l@Fn@D|@F~@Dl@Bh@B`CDzAFpBJh@Dd@D^Fd@J\\\\J^Ll@Tx@\\\\`Br@zAn@|@`@@b@hAd@vBz@pB|@`A`@`@P`@L^N\\\\JXFXDTBTBX@X@\\\\AT?VATCZCZG^Ib@KxC{@pEqAjCw@`Cq@VC\\\\GZEb@Ef@CZC@L`@FZFRBL?N@LAL?JCTGZKZKNELEJANCR?PAd@An@?^ANAJAHALEHCJGJGXWDEZUNMLILGBAHCNERCPANAN?J@F@P@ZF^Dj@LTFVFVHr@VlAb@bA\\\\RFVFTFPFRDRBVDh@FZBfAJbBL@?jBPb@DbAHbBNfBN^DP@N@L?P@P?P?P?TAZCVAVCXE`@EZELAJAJ?J?J?XB`@BxALN@f@Dr@Fb@BZ@`@@X@R?PAVAZA`AG|BOxCQ`BMfAG`AGLAnBK\\\\CLAVAR?L@VBh@Dj@Fv@Ht@Fj@DRBTBXBVBXBXDTFRFh@Nh@NJBNDTDRBTDRBT@P@R?T?V?TA\\\\Cf@Gv@K~@M\\\\EVAPAP?T?P@P@V@PBPDRDjAVdCj@nA\\\\h@P^N^PZRVNRNPNPLb@b@fAfAjBhBjAhAhAhAz@x@h@f@b@b@VTPNPNTLVPf@Xd@Xh@\\\\XPRNNLRRTZ\\\\b@Zb@PTRRNLPNXPZPZPZP\\\\NVJRFPFPBVDTBRBV@V@Z?ZAd@Az@ElDO|DQh@Ch@Ed@E\\\\E`@Ej@In@Kn@MtAYbDw@r@Q\\\\GVEZC^Cf@CR?xCM~BKrAEp@ATAV@V?XBXBTDVFZHVHRHTJr@\\\\XNZLTJRFRFPDTDVDRBV@V@V@ZAn@Af@Eb@AXAXAV?Z?`A?v@?fA@\\\\?P@N@L@PBJDJBLFLFRNDBVTnAnA`A~@^\\\\VRRNPLDBfAr@jAv@j@\\\\JDLFLDN@HBF?HAHAHCLGHGPOHGHEHCLAL@N@NDHFJFJLJNFNBLBT@V@f@?|D?fB?|@APARAF?BAB?@ABAFEHELELILINIPIPGTEZG\\\\G\\\\Id@EVENEHGHGFMFKDMBOBSBm@D_@Da@FUDSFQFKFKFKFIHk@j@GDGDKFMDMBODQDWFKBKDIFEFAHAH?F@JFTHXFRBLBJ?J@L?RA^?T?R@RBXDf@XxBBVFJDJHFLFNDZJTJJFDDBFBJ@J?R?dA?NAJAJEHCFIHIDOBSBU@Y?O?QAKAICIAGCKG[IIAGAG?GBEDCD?F?HDHFHXT^Z\\\\VHHFJBHBH?L?PAP?H@D?DBHDHJRV\\\\X\\\\\\\\`@LNJHJHLHZPVPFHFFDJDRJ^DJFJHFHDJBL@XBVBVBRDTHb@Px@ZZJLDJBH@H@H?HAHAJE\\\\KPGJCHAJ?J@JBJHRPXXLNDHBFBH?F?H?HCHCHGLKPk@@INIHIFIFIDIBIBK@M?O@M?M?I?IBGBCDCFCF?F?D@HBFFFJHLLHJDJBFBH@H?J?FAFCJEHKNm@b@q@f@u@h@KJGHCFAH?L?JBJDJDJBH@H@J?HAH?HCJCHGJEFIH}@b@e@TOHIFEFEFCH?H@H@JDJDLDFFHFFJHHFFDJDLDPDZHVFZH^J^Nd@Rh@Vd@Vl@^t@b@BBHD^Tb@T@h@RJ\\\\Pd@Xd@T^PZLjAb@vD|ARHHJDHFLBNDPJ`@@JHVHVXt@lBxDx@dBh@pAVj@tAfDpA`DDLb@dAl@xAPb@z@tBZv@lBxEv@lBtBbFRh@f@hAlAtCl@tAVj@R^T^T^VZX\\\\VXVVb@\\\\h@`@dHlEXVJNHPN`@DLJLJHLDNBP?Z@PBLDLD~A~@fBlAbAn@bC|An@b@r@f@x@j@x@h@z@l@vA|@`BdAtBpAd@\\\\HFnGvElF`Dz@h@r@b@z@h@n@`@jHvEr@d@pAx@PJfAnAv@f@xEzC?F@DBDBDBDD@n@b@t@h@t@d@r@f@p@f@PJ|Al@d@DPJXR~@l@vDhCl@`@NJNJVNNLNLb@f@TPzAbAj@^jAv@v@f@NHx@n@n@\\\\bBn@?H@JBHBBDDHDJBL@NELIFM`BDhB@rBOfC?\\\\CVCPEREp@YbAc@PGLCNANAL?V@zBh@fB`@LB~WvGzFxAPD~A`@nBLbAD~@FTBLBdC`BdAp@l@b@|@h@h@\\\\FD|@p@XZTf@HRF^?b@C^Qf@y@vBm@zAG\\\\CV@TBXFXDLDJJLHJIpBQfDGfAK`BSjDSjDkDSgDSC?cDSeDSPmDDs@FeA@k@qAI',\n" +
                    "      '}y}]r~}kMM@q@@A?i@@QSa@Sh@qAZ{@R_@IiAo@s@kBM{@eDWcAEmEA{@r@i@z@GjAKx@]BCbBoA^IdCCdCEzBCpAE^KPCd@GPCDCDAFCJODM@K?U?c@?KEcC?MAGEMCICOEIMK]MIGEEGMCcC?KEyBE}BEeCAm@AmAAw@?w@BO\\\\k@BC|@}@`@e@lBuBHIj@m@VYTWTWLM|@aARSNWJQFW?W?g@CgC?S@O@QFk@@QNgB@W@KD]NcANe@FOHMPULMHG^SVMNG`@YPMpAoALOHQFMF[@QIuAJiF@QBU@GTw@DK@K@MFyB@]Fa@Fu@Fy@@MB]BSHeABSFQDOdAkCXs@F_ALyB@U@I@G@IBIRSHMh@{@b@q@J]@E@K?C@K@M?E@O@k@@M?q@?KDwB@s@@]@SBSFULUPSFG~@u@xBkBFEp@i@XWb@a@R[AaEA_A@@Hc@J]LWr@oAJWZq@Pc@BGHWFSH@v@oED_@?iA@aA?s@?Y?SF_@DSHQ@CBEVc@d@k@^c@VQLGNGPGl@MXIRKd@Ud@W`Am@l@UdAQj@Iz@M',\n" +
                    "    'u{|]rs{kM}@B@f@BbBFfCeC@EcCA{@Ag@?i@As@Da@L[HIj@m@VYTWTWLM|@aARSNWJQFW?W?g@Hs@JWVo@Ra@T_@HMLKVM^OVOBCJMFGBGJSBGTa@\\\\g@~@u@LIJGX]DGLQb@o@HELGLGbASJ@l@Vl@R\\\\D`@@RFl@\\\\`Ab@VLb@RN@H?dAKNEHIDQ@QCQEMKIk@Qu@SY[Nw@B]DW@IDUJMHKZWf@m@FKFOFa@@MBMLQhA{@RGTAPBJJRRHd@HVLp@@BDZLt@Vx@TJR?l@Cp@Cf@Vl@dAZPVDZGPKBEDMHO']", 5);
            style.addSource(new GeoJsonSource("simplifiedLine", Feature.fromGeometry(LineString.fromLngLats(points))));
            style.addLayer(new LineLayer("simplifiedLine", "simplifiedLine").withProperties(
                    lineColor(colorLine),
                    lineWidth(4f)
            ));

        }
        currentPosition = new LatLng(points.get(0).latitude(),points.get(0).longitude());
        geoJsonSource = new GeoJsonSource("source-id",
                Feature.fromGeometry(Point.fromLngLat(points.get(0).longitude(),
                        points.get(0).latitude())));

        style.addImage(("marker_icon"), BitmapFactory.decodeResource(
                getResources(), R.drawable.mapbox_marker_icon_default));

        style.addSource(geoJsonSource);

        style.addLayer(new SymbolLayer("layer-id", "source-id")
                .withProperties(
                        PropertyFactory.iconImage("marker_icon"),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                ));
        initRunnable();
    }

    private void positionUser() {
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(points.get(count).latitude(), points.get(count).longitude()))
                .zoom(16) // Sets the zoom
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);

    }

    private void onZoom(double zoom) {
        CameraPosition position = new CameraPosition.Builder()
                .zoom(mapboxMap.getCameraPosition().zoom + zoom).
                        build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
    }

    public void onEmergencia() {

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @SuppressLint("MissingPermission")
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {

            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(getContext(), loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setLocationComponentEnabled(false);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            Location location = mapboxMap.getLocationComponent().getLastKnownLocation();
            Latitude = location.getLatitude();
            Longitude = location.getLongitude();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            animator.cancel();
        }
        if (mapboxMap != null) {
//            mapboxMap.removeOnMapClickListener(this);
        }
        mapView.onDestroy();
    }
}

