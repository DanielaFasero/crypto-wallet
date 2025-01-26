package eu.assessment.swisspost.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers;
import eu.assessment.swisspost.user.domain.entity.User;
import eu.assessment.swisspost.user.repository.UserRepository;
import eu.assessment.swisspost.utils.PostgresSQLContainerTestingSupport;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class UserManagementControllerITTest implements PostgresSQLContainerTestingSupport {

  @Autowired private UserRepository userRepository;
  @Autowired private MockMvc mockMvc;

  private final String yaml =
      Objects.requireNonNull(this.getClass().getClassLoader().getResource("open-api.yaml"))
          .getPath();

  @AfterEach
  void tearDown() {
    this.userRepository.deleteAll();
  }

  @Test
  public void shouldCreateUserWhenValidEmailIsGiven() throws Exception {

    assertThat(this.userRepository.findAll().size()).isZero();

    String contentCreationUser =
        """
                {
                	"email": "thisIsAnEmail@emalSomething.stuffs"
                }
                """;

    this.mockMvc
        .perform(
            post("/users/createUser")
                .content(contentCreationUser)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(200))
        .andExpect(OpenApiValidationMatchers.openApi().isValid(this.yaml));

    assertThat(this.userRepository.findAll().size()).isEqualTo(1);
  }

  @Test
  public void shouldNotCreateUserWhenAlreadyExistingEmailIsGiven() throws Exception {
    this.userRepository.save(new User("thisIsAnEmail@emalSomething.stuffs"));

    assertThat(this.userRepository.findAll().size()).isGreaterThan(0);

    String contentCreationUser =
        """
                    {
                        "email": "thisIsAnEmail@emalSomething.stuffs"
                    }
                    """;

    this.mockMvc
        .perform(
            post("/users/createUser")
                .content(contentCreationUser)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(208))
        .andExpect(OpenApiValidationMatchers.openApi().isValid(this.yaml));

    assertThat(this.userRepository.findAll().size()).isEqualTo(1);
  }

  @Test
  public void shouldReturnTotalWalletInformationWhenValidUserIsGiven() throws Exception {
    this.userRepository.save(new User("thisIsAnEmail@emalSomething.stuffs"));

    assertThat(this.userRepository.findAll().size()).isGreaterThan(0);

    String totalUserInfoRequest =
        """
                    {
                        "email": "thisIsAnEmail@emalSomething.stuffs"
                    }
                    """;

    this.mockMvc
        .perform(
            post("/users/total")
                .content(totalUserInfoRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(200))
        .andExpect(OpenApiValidationMatchers.openApi().isValid(this.yaml));

    assertThat(this.userRepository.findAll().size()).isEqualTo(1);
  }

  @Test
  public void shouldNotReturnTotalWalletInformationWhenNoValidUserIsGiven() throws Exception {

    assertThat(this.userRepository.findAll().size()).isZero();

    String totalUserInfoRequest =
        """
                    {
                        "email": "thisIsAnEmail@emalSomething.stuffs"
                    }
                    """;

    this.mockMvc
        .perform(
            post("/users/total")
                .content(totalUserInfoRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(400))
        .andExpect(OpenApiValidationMatchers.openApi().isValid(this.yaml));

    assertThat(this.userRepository.findAll().size()).isZero();
  }
}
