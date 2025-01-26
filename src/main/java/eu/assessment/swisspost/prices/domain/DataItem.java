package eu.assessment.swisspost.prices.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"time", "date"})
public record DataItem(Double priceUsd) {}
