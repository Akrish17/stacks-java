package com.xxAMIDOxx.xxSTACKSxx.provider.azure;

import com.xxAMIDOxx.xxSTACKSxx.menu.domain.AzureMenu;
import com.xxAMIDOxx.xxSTACKSxx.menu.repository.MenuRepositoryAdapter;
import com.xxAMIDOxx.xxSTACKSxx.menu.service.MenuQueryService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(name = "cloud-provider", havingValue = "azure")
@Service("menuQueryService")
public class CosmosMenuQueryService implements MenuQueryService {

  private static final String NAME = "name";

  private static Logger logger = LoggerFactory.getLogger(CosmosMenuQueryService.class);

  private MenuRepositoryAdapter menuRepositoryAdapter;

  @Autowired
  public CosmosMenuQueryService(MenuRepositoryAdapter menuRepositoryAdapter) {
    this.menuRepositoryAdapter = menuRepositoryAdapter;
  }

  public Optional<AzureMenu> findById(UUID id) {
    return menuRepositoryAdapter.findById(id.toString());
  }

  public List<AzureMenu> findAll(int pageNumber, int pageSize) {

    Page<AzureMenu> page =
        menuRepositoryAdapter.findAll(PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, NAME)));

    // This is specific and needed due to the way in which CosmosDB handles pagination
    // using a continuationToken and a limitation in the Swagger Specification.
    // See https://github.com/Azure/azure-sdk-for-java/issues/12726
    int currentPage = 0;
    while (currentPage < pageNumber && page.hasNext()) {
      currentPage++;
      Pageable nextPageable = page.nextPageable();
      page = menuRepositoryAdapter.findAll(nextPageable);
    }
    return page.getContent();
  }

  @Override
  public List<AzureMenu> findAllByRestaurantId(UUID restaurantId, Integer pageSize, Integer pageNumber) {
    return menuRepositoryAdapter
        .findAllByRestaurantId(
            restaurantId.toString(), PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, NAME)))
        .getContent();
  }

  @Override
  public List<AzureMenu> findAllByNameContaining(String searchTerm, Integer pageSize, Integer pageNumber) {
    return menuRepositoryAdapter
        .findAllByNameContaining(
            searchTerm, PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, NAME)))
        .getContent();
  }

  @Override
  public List<AzureMenu> findAllByRestaurantIdAndNameContaining(UUID restaurantId, String searchTerm, Integer pageSize, Integer pageNumber) {
    return menuRepositoryAdapter
        .findAllByRestaurantIdAndNameContaining(
            restaurantId.toString(),
            searchTerm,
            PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, NAME)))
        .getContent();
  }
}