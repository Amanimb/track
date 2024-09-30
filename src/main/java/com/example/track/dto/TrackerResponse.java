package com.example.track.dto;

import java.time.LocalDate;

public record TrackerResponse(String emailId, LocalDate sentOn, boolean isSent, boolean isRead) {
}
