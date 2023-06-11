package org.okcoder.sample.spring_boot_app_logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class TestController {

    private Logger logger = LoggerFactory.getLogger(TestController.class);
    @GetMapping("/")
    public Object hello(){
        return System.currentTimeMillis();
    }

    @PostMapping("/")
    public Object body(@RequestBody String body){
        logger.info("{}",body);
        return body;
    }

    @PostMapping(value = "/multi-part",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //curl -X POST -F 'request={"a":1};type=application/json' -F file=@gradlew.bat localhost:8080/multi-part -v
    public Object multiPart(@RequestPart(value = "file",required = false)MultipartFile file,
                            @RequestPart(value = "request",required = false) Map<String,Object> request){
        logger.info("{},{}",file,request);
        return request;
    }

}
