package eu.assessment.swisspost.wallet.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.assessment.swisspost.prices.repository.PriceRepository;
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
class AssetManagementControllerITTest implements PostgresSQLContainerTestingSupport {
  @Autowired private UserRepository userRepository;
  @Autowired private PriceRepository priceRepository;
  @Autowired private MockMvc mockMvc;

  private final String yaml =
      Objects.requireNonNull(this.getClass().getClassLoader().getResource("open-api.yaml"))
          .getPath();

  @AfterEach
  void tearDown() {
    this.userRepository.deleteAll();
  }

  @Test
  public void shouldAssetsBeSentToEvaluateThenReturnOk() throws Exception {

    await().untilAsserted(() -> assertThat(priceRepository.findAll().size()).isEqualTo(100));

    String request =
        """
            {
              "assets": [
                {
                  "symbol": "BTC",
                  "quantity": 0.5,
            			"price": 35000
            		},
                {
                  "symbol": "ETH",
                  "quantity": 4.25,
            			"price": 15310.71
                },
                {
                  "symbol": "WBTC",
                  "quantity": 8,
            			"price": 104907.316
                }
              ],
              "startDate": "01/01/2025",
              "endDate": "24/01/2025"
            }
            """;
    this.mockMvc
        .perform(
            post("/assets/walletEvaluation")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void shouldUnknownAssetsBeSentToEvaluateThenReturnBadRequest() throws Exception {

    await().untilAsserted(() -> assertThat(priceRepository.findAll().size()).isEqualTo(100));

    String request =
        """
                {
                  "assets": [
                    {
                      "symbol": "POTATOES",
                      "quantity": 0.5,
                            "price": 35000
                        }
                  ],
                  "startDate": "01/01/2025",
                  "endDate": "24/01/2025"
                }
                """;
    this.mockMvc
        .perform(
            post("/assets/walletEvaluation")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldAddAssetsToExistingUsersWalletThenReturnOk() throws Exception {
    this.userRepository.save(new User("thisIsAnEmail@emalSomething.stuffs"));

    assertThat(this.userRepository.findAll().size()).isGreaterThan(0);
    await().untilAsserted(() -> assertThat(priceRepository.findAll().size()).isEqualTo(100));

    String request =
        """
                {
                    "userEmail": "thisIsAnEmail@emalSomething.stuffs",
                    "symbol": "dot",
                    "price": "11",
                    "quantity":"1900"
                }
                """;
    this.mockMvc
        .perform(
            post("/assets/addAsset").content(request).contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  // todo: add bad request use case
}
