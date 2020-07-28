package com.xxAMIDOxx.xxSTACKSxx.menu.api.v1.impl;

import com.microsoft.azure.spring.autoconfigure.cosmosdb.CosmosAutoConfiguration;
import com.microsoft.azure.spring.autoconfigure.cosmosdb.CosmosDbRepositoriesAutoConfiguration;
import com.xxAMIDOxx.xxSTACKSxx.core.api.dto.ErrorResponse;
import com.xxAMIDOxx.xxSTACKSxx.menu.api.v1.dto.request.CreateCategoryRequest;
import com.xxAMIDOxx.xxSTACKSxx.menu.api.v1.dto.response.ResourceCreatedResponse;
import com.xxAMIDOxx.xxSTACKSxx.menu.domain.Category;
import com.xxAMIDOxx.xxSTACKSxx.menu.domain.Menu;
import com.xxAMIDOxx.xxSTACKSxx.menu.repository.MenuRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static com.azure.data.cosmos.internal.Utils.randomUUID;
import static com.xxAMIDOxx.xxSTACKSxx.menu.domain.MenuHelper.createMenu;
import static com.xxAMIDOxx.xxSTACKSxx.util.TestHelper.getBaseURL;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
        exclude = {
                CosmosDbRepositoriesAutoConfiguration.class,
                CosmosAutoConfiguration.class
        })
@Tag("Integration")
class CreateCategoryControllerImplTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private MenuRepository menuRepository;

    @Test
    void testCanNotAddCategoryIfMenuNotPresent() {
        // Given
        UUID menuId = randomUUID();
        when(menuRepository.findById(eq(menuId.toString())))
                .thenReturn(Optional.empty());

        CreateCategoryRequest request = new CreateCategoryRequest("test Category Name",
                "test Category Description");

        // When
        var response =
                this.testRestTemplate.postForEntity(
                        String.format("%s/v1/menu/%s/category", getBaseURL(port), menuId),
                        request,
                        ErrorResponse.class);

        // Then
        then(response).isNotNull();
        then(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testInvalidRequestObjectGivenReturnsBadRequest() {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest("test Category Name",
                "");

        // When
        var response =
                this.testRestTemplate.postForEntity(
                        String.format("%s/v1/menu/%s/category", getBaseURL(port), randomUUID()),
                        request,
                        ErrorResponse.class);

        // Then
        then(response).isNotNull();
        then(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    void testInvalidMenuIdWilThrowBadRequest() {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest("test Category Name",
                "");

        // When
        var response =
                this.testRestTemplate.postForEntity(
                        String.format("%s/v1/menu/%s/category", getBaseURL(port), "XXXXXX"),
                        request,
                        ErrorResponse.class);

        // Then
        then(response).isNotNull();
        then(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    void testAddCategory() {
        // Given
        Menu menu = createMenu(1);
        when(menuRepository.findById(eq(menu.getId())))
                .thenReturn(Optional.of(menu));
        when(menuRepository.save(any(Menu.class))).thenReturn(menu);

        CreateCategoryRequest request = new CreateCategoryRequest("test Category Name",
                "test Category Description");

        // When
        var response =
                this.testRestTemplate.postForEntity(
                        String.format("%s/v1/menu/%s/category", getBaseURL(port), menu.getId()),
                        request,
                        ResourceCreatedResponse.class);

        // Then
        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository, times(1)).save(captor.capture());
        Menu created = captor.getValue();

        then(created.getName()).isEqualTo(menu.getName());
        then(created.getDescription()).isEqualTo(menu.getDescription());
        then(created.getCategories().size()).isEqualTo(1);
        then(created.getCategories().get(0).getName()).isEqualTo(request.getName());
        then(created.getCategories().get(0).getDescription()).isEqualTo(request.getDescription());

        then(response).isNotNull();
        then(response.getBody()).isNotNull();
        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(created.getCategories().get(0).getId()).isEqualTo(response.getBody().getId().toString());
    }

    @Test
    void testCannotAddCategoryWhichAlreadyExists() {
        // Given
        Menu menu = createMenu(1);
        Category category = new Category(UUID.randomUUID().toString(),
                "cat name",
                "cat description",
                new ArrayList<>());
        menu.addUpdateCategory(category);

        when(menuRepository.findById(eq(menu.getId())))
                .thenReturn(Optional.of(menu));

        CreateCategoryRequest request = new CreateCategoryRequest(category.getName(),
                "test Category Description");

        // When
        var response =
                this.testRestTemplate.postForEntity(
                        String.format("%s/v1/menu/%s/category", getBaseURL(port), menu.getId()),
                        request,
                        ErrorResponse.class);

        // Then
        then(response).isNotNull();
        then(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}