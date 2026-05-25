package com.fengzhi.weather.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.fengzhi.weather.data.api.NictApi
import com.fengzhi.weather.data.api.QWeatherApi
import com.fengzhi.weather.data.api.RainViewerApi
import com.fengzhi.weather.data.api.WeatherApi
import com.fengzhi.weather.data.local.CityPreferences
import com.fengzhi.weather.data.local.SettingsPreferences
import com.fengzhi.weather.data.repository.RadarRepository
import com.fengzhi.weather.data.repository.SatelliteRepository
import com.fengzhi.weather.data.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideCityPreferences(dataStore: DataStore<Preferences>): CityPreferences {
        return CityPreferences(dataStore)
    }

    @Provides
    @Singleton
    fun provideSettingsPreferences(dataStore: DataStore<Preferences>): SettingsPreferences {
        return SettingsPreferences(dataStore)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.weather.com/") // Placeholder URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApi(retrofit: Retrofit): WeatherApi {
        return retrofit.create(WeatherApi::class.java)
    }

    /**
     * 提供 NICT API 接口实例
     * 用于获取向日葵卫星云图数据
     */
    @Provides
    @Singleton
    fun provideNictApi(): NictApi {
        return NictApiImpl()
    }

    /**
     * 提供和风天气 API 接口实例
     */
    @Provides
    @Singleton
    fun provideQWeatherApi(okHttpClient: OkHttpClient): QWeatherApi {
        return Retrofit.Builder()
            .baseUrl("https://devapi.qweather.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QWeatherApi::class.java)
    }

    /**
     * 提供 RainViewer API 接口实例
     */
    @Provides
    @Singleton
    fun provideRainViewerApi(okHttpClient: OkHttpClient): RainViewerApi {
        return Retrofit.Builder()
            .baseUrl("https://api.rainviewer.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RainViewerApi::class.java)
    }

    /**
     * 提供天气数据仓库
     */
    @Provides
    @Singleton
    fun provideWeatherRepository(qWeatherApi: QWeatherApi): WeatherRepository {
        return WeatherRepository(qWeatherApi)
    }

    /**
     * 提供雷达数据仓库
     */
    @Provides
    @Singleton
    fun provideRadarRepository(rainViewerApi: RainViewerApi): RadarRepository {
        return RadarRepository(rainViewerApi)
    }

    /**
     * 提供卫星数据仓库
     */
    @Provides
    @Singleton
    fun provideSatelliteRepository(
        @ApplicationContext context: Context,
        nictApi: NictApi
    ): SatelliteRepository {
        return SatelliteRepository(context, nictApi)
    }
}

/**
 * NictApi 的实现类
 * 由于 NICT API 使用动态 URL 构建，需要自定义实现
 */
private class NictApiImpl : NictApi {
    override suspend fun getLatestTimestamp(): retrofit2.Response<com.fengzhi.weather.data.model.SatelliteTimestamp> {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val request = okhttp3.Request.Builder()
            .url("${NictApi.BASE_URL}himawari8/img/D531106/latest.json")
            .build()

        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            return retrofit2.Response.error(response.code, response.body ?: "".toResponseBody())
        }

        val body = response.body?.string() ?: return retrofit2.Response.error(500, "".toResponseBody())
        
        val gson = com.google.gson.Gson()
        val timestamp = gson.fromJson(body, com.fengzhi.weather.data.model.SatelliteTimestamp::class.java)
        
        return retrofit2.Response.success(timestamp)
    }
}
