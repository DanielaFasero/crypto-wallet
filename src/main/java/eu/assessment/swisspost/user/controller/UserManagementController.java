package eu.assessment.swisspost.user.controller;

import eu.assessment.swisspost.user.domain.UserCreationRequest;
import eu.assessment.swisspost.user.domain.UserCreationResponse;
import eu.assessment.swisspost.user.domain.UserInformationRequest;
import eu.assessment.swisspost.user.domain.UserInformationResponse;
import eu.assessment.swisspost.user.service.UserManagementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@Slf4j
@AllArgsConstructor
public class UserManagementController {

  private final UserManagementService userManagementService;

  @PostMapping(value = "/createUser", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserCreationResponse> createUser(
      @RequestBody UserCreationRequest userCreationRequest) {
    UserCreationResponse response = userManagementService.createUser(userCreationRequest);

    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/total", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserInformationResponse> totalInformationOfUserWallet(
      @RequestBody UserInformationRequest userCreationRequest) {
    UserInformationResponse totalInformationOfUser =
        userManagementService.getTotalInformationOfUser(userCreationRequest);
    return ResponseEntity.ok(totalInformationOfUser);
  }
}
