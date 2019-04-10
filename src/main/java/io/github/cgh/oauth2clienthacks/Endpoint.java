package io.github.cgh.oauth2clienthacks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class Endpoint {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate;

    @Autowired
    public Endpoint(OAuth2AuthorizedClientService authorizedClientService, RestTemplate restTemplate) {
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public String get(Model model, OAuth2AuthenticationToken token) {
        model.addAttribute("user", fetchUserInfo(token));
        return "index";
    }

    private UserInfo fetchUserInfo(OAuth2AuthenticationToken token) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(token.getAuthorizedClientRegistrationId(), token.getName());
        String uri = client.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue());
        HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<UserInfo> response = restTemplate.exchange(uri, HttpMethod.GET, entity, UserInfo.class);
        return response.getBody();
    }

    public static class UserInfo {
        public String name;
        public String picture;
        public String email;

        public UserInfo() {
        }
    }
}
