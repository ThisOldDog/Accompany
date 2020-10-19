package pers.teemo.accompany.asset;

import android.content.res.AssetManager;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pers.teemo.accompany.util.YamlUtils;

@SuppressWarnings("unchecked")
public class AssetLoader {
    private static final Logger logger = LoggerFactory.getLogger(AssetLoader.class);

    private static final Map<Class<?>, Object> INSTANCE_CACHE = new ConcurrentHashMap<>(16);
    private static final Map<Class<?>, AssetInfo> ENTITY_ASSET = new ConcurrentHashMap<>(16);
    private static final String ASSET_SUFFIX = ".yml";

    public static  <T> T getInstance(AssetManager assetManager, Class<T> assetClass) {
        if (INSTANCE_CACHE.containsKey(assetClass)) {
            return (T) INSTANCE_CACHE.get(assetClass);
        }
        try (InputStream assetInputStream = assetManager.open(getAssetName(assetClass))) {
            T assetInstance = YamlUtils.parser(assetInputStream, assetClass);
            INSTANCE_CACHE.put(assetClass, assetInstance);
            return assetInstance;
        } catch (IOException e) {
            logger.error("An error occurred while reading the properties file.", e);
            throw new RuntimeException("Unable to read yaml : " + getAssetName(assetClass));
        }
    }

    private static String getAssetName(Class<?> assetClass) {
        AssetInfo assetInfo = ENTITY_ASSET.get(assetClass);
        if (assetInfo != null) {
            return assetInfo.getName();
        }
        if (assetClass.isAnnotationPresent(Asset.class)) {
            Asset asset = assetClass.getAnnotation(Asset.class);
            assetInfo = new AssetInfo()
                    .setName(asset.name() + ASSET_SUFFIX);
        } else {
            assetInfo = new AssetInfo()
                    .setName(assetClass.getSimpleName() + ASSET_SUFFIX);
        }
        ENTITY_ASSET.put(assetClass, assetInfo);
        return assetInfo.getName();
    }

}
