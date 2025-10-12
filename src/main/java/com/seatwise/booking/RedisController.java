package com.seatwise.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis-template")
@RequiredArgsConstructor
public class RedisController {

  private final RedisTemplateBenchmarkService redisService;

  @PostMapping("/set")
  public String set(@RequestParam String key, @RequestParam String value) {
    redisService.setValue(key, value);
    return "OK";
  }

  @GetMapping("/get")
  public String get(@RequestParam String key) {
    String result = redisService.getValue(key);
    return result != null ? result : "NOT_FOUND";
  }
}
