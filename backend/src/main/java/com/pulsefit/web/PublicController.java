package com.pulsefit.web;

import com.pulsefit.service.CatalogService;
import com.pulsefit.web.dto.ApiDtos.LandingResponse;
import com.pulsefit.web.dto.ApiDtos.SessionSummary;
import com.pulsefit.web.dto.ApiDtos.StudioDetailResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PublicController {
  private final CatalogService catalogService;

  @GetMapping("/landing")
  LandingResponse landing() {
    return catalogService.landing();
  }

  @GetMapping("/sessions")
  List<SessionSummary> sessions(@RequestParam(required = false) String q) {
    return catalogService.discover(q);
  }

  @GetMapping("/studios/{slug}")
  StudioDetailResponse studio(@PathVariable String slug) {
    return catalogService.studioDetail(slug);
  }
}
