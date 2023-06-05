package org.okcoder.sample.spring_boot_app_logging;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public Object hello(){
        return System.currentTimeMillis();
    }

    @PostMapping("/")
    public Object body(@RequestBody String body){
        return body;
    }
}
