package org.adrianwalker.startstopcontinue.dataaccess;

import io.minio.MinioClient;
import org.adrianwalker.startstopcontinue.configuration.DataConfiguration;

public final class DataAccessFactory {

  private final DataConfiguration config;

  public DataAccessFactory(final DataConfiguration config) {

    this.config = config;
  }

  public DataAccess create() {

    DataAccess dataAccess;

    if (!config.getDataEndpoint().isEmpty() && config.getDataPort() > 0) {

      dataAccess = new MinioDataAccess(createMinioClient(config), config.getDataBucket());

    } else {

      dataAccess = new JsonFileSystemDataAccess(config.getDataPath());
    }

    return dataAccess;
  }

  private static MinioClient createMinioClient(final DataConfiguration config) throws RuntimeException {

    MinioClient minioClient;
    try {
      minioClient = new MinioClient(
        config.getDataEndpoint(), config.getDataPort(),
        config.getDataAccessKey(), config.getDataSecretKey(),
        config.getDataRegion(), config.getDataSecure());

    } catch (final Exception e) {
      throw new RuntimeException(e);
    }

    return minioClient;
  }
}
