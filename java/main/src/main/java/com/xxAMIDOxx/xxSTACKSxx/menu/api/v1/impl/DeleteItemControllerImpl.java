package com.xxAMIDOxx.xxSTACKSxx.menu.api.v1.impl;

import com.xxAMIDOxx.xxSTACKSxx.menu.api.v1.DeleteItemController;
import com.xxAMIDOxx.xxSTACKSxx.menu.commands.DeleteItemCommand;
import com.xxAMIDOxx.xxSTACKSxx.menu.handlers.DeleteItemHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

/** @author ArathyKrishna */
@RestController
public class DeleteItemControllerImpl implements DeleteItemController {

  private DeleteItemHandler handler;

  public DeleteItemControllerImpl(DeleteItemHandler handler) {
    this.handler = handler;
  }

  @Override
  public ResponseEntity<Void> deleteItem(
      UUID menuId, UUID categoryId, UUID itemId, String correlationId) {
    handler.handle(new DeleteItemCommand(correlationId, menuId, categoryId, itemId));
    return new ResponseEntity<>(OK);
  }
}