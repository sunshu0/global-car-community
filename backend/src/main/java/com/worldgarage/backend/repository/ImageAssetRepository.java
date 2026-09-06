package com.worldgarage.backend.repository;

import com.worldgarage.backend.model.ImageAsset;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageAssetRepository
    extends JpaRepository<ImageAsset, UUID> {}
