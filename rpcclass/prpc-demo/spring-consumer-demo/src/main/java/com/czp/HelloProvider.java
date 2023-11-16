package com.czp;

import com.czp.annotation.PrpcBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloProvider {


    @PrpcBean
    private HelloRpc helloRpc;
    @GetMapping("consumer")
    public String hello(){
        return helloRpc.sayHi("你好啊");
    }

}
