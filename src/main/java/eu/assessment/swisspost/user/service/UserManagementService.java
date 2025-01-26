package eu.assessment.swisspost.user.service;

import eu.assessment.swisspost.user.domain.*;
import eu.assessment.swisspost.user.domain.entity.User;
import eu.assessment.swisspost.user.repository.UserRepository;
import eu.assessment.swisspost.utils.exceptions.UserAlreadyExistsException;
import eu.assessment.swisspost.utils.exceptions.UserNotFoundException;
import eu.assessment.swisspost.wallet.domain.entity.Asset;
import eu.assessment.swisspost.wallet.domain.entity.Wallet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserManagementService {

  private final UserRepository userRepository;

  public UserCreationResponse createUser(UserCreationRequest userCreationRequest) {
    Optional<User> existingUser = userRepository.findByEmail(userCreationRequest.email());

    if (existingUser.isPresent()) {
      throw new UserAlreadyExistsException(userCreationRequest.email());
    }
    return createNewUser(userCreationRequest.email());
  }

  public UserInformationResponse getTotalInformationOfUser(
      UserInformationRequest userInformationRequest) {

    Optional<User> userFromDB = this.userRepository.findByEmail(userInformationRequest.email());

    if (userFromDB.isPresent()) {
      return new UserInformationResponse(
          "We found the following information for the user",
          convertWalletToDTO(userFromDB.get()),
          HttpStatus.OK);
    } else throw new UserNotFoundException(userInformationRequest.email());
  }

  private WalletDTO convertWalletToDTO(User user) {
    Wallet wallet = user.getWallet();
    Set<Asset> assets = user.getWallet().getAssets();

    Set<AssetDTO> assetDTOs =
        assets.stream()
            .map(
                asset ->
                    new AssetDTO(
                        asset.getSymbol(),
                        asset.getQuantity(),
                        asset.getPrice(),
                        asset.getCurrentValue()))
            .collect(Collectors.toSet());
    return new WalletDTO(wallet.getId(), wallet.getTotal(), assetDTOs);
  }

  private UserCreationResponse createNewUser(String email) {

    Optional<User> optionalOfSaved = Optional.of(this.userRepository.save(new User(email)));

    return optionalOfSaved
        .map(savedUser -> new UserCreationResponse(convertUserToDTO(savedUser)))
        .get();
  }

  private UserDTO convertUserToDTO(User savedUser) {
    Wallet savedWallet = savedUser.getWallet();
    WalletDTO walletDTO = new WalletDTO(savedWallet.getId(), savedWallet.getTotal(), null);

    return new UserDTO(savedUser.getId(), savedUser.getEmail(), walletDTO);
  }
}
