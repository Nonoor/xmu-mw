package cn.edu.xmu.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import cn.edu.xmu.util.MD5;
import cn.edu.xmu.util.TranslateData;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class TransApi {
    private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";

    @Value("${baidu_translate.appid}")
    private String appid;
    @Value("${baidu_translate.securityKey}")
    private String securityKey;

    // 添加了一个无参构造器，不然无法注入这个bean，否则需要额外的配置。
    public TransApi() {}

    @Autowired
    private RestTemplate restTemplate;

    public TransApi(String appid, String securityKey) {
        this.appid = appid;
        this.securityKey = securityKey;
    }

    public String getTransResult(String query, String from, String to) {
        Map<String, String> params = buildParams(query, from, to);
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.setAll(params);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(TRANS_API_HOST);
        URI uri = builder.queryParams(requestParams).build().encode().toUri();
        System.out.println("uri: " + uri);
        System.out.println(restTemplate.getForObject(uri, String.class));
        String json = restTemplate.getForObject(uri, String.class);
        Gson gson = new Gson();
        TranslateData data = gson.fromJson(json,TranslateData.class);
        StringBuffer dst = new StringBuffer(100);
        for(int i = 0;i<data.getTrans_result().size() - 1;i++){
            dst.append(data.getTrans_result().get(i).getDst()+'\n');
        }
        dst.append(data.getTrans_result().get(data.getTrans_result().size() - 1).getDst());
        return dst.toString();
    }

    private Map<String, String> buildParams(String query, String from, String to) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);

        params.put("appid", appid);

        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);

        // 签名
        String src = appid + query + salt + securityKey; // 加密前的原文
        params.put("sign", MD5.md5(src));

        return params;
    }

}