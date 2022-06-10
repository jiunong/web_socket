package com.xcloud.svg.serviceBus;

import com.alibaba.fastjson.JSONObject;
import com.ddxx.cw.util.ServiceUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2021/10/9 14:21
 */
@RestController
public class ServiceBusController {

    @GetMapping(value = "/test")
    public String hello(HttpServletRequest request) { String date = request.getParameter("date");
        String userRole = request.getParameter("userRole");
        JSONObject param = new JSONObject();
        param.put("date", date);
        param.put("userRole", userRole);
        System.out.println("paramparamparamparamparamparam#################" + param.toJSONString());
        String responseString = ServiceUtil.doService("1446232131452829697", param.toJSONString());
        System.out.println("responseStringresponseStringresponseStringresponseString#################" + responseString);
        Map map = (Map)JSONObject.parseObject(responseString, Map.class);
        return map.get("value").toString();
    }

}
