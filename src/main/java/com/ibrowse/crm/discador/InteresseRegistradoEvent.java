package com.ibrowse.crm.discador;

/** Publicado quando um interesse com telefone é salvo; o envio à Vonix acontece depois do commit. */
public record InteresseRegistradoEvent(Long interesseId) {
}
