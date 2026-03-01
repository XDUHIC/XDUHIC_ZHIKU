package com.example.backend.service;

public interface PointService {
    int getBalance(Long userId);
    void addPoints(Long userId, int amount, String reason, String refType, Long refId);
}
