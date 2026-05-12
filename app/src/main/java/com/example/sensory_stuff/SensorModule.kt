package com.example.sensory_stuff

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LightSensorQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AccelerometerQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HeadingSensorQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TempSensorQualifier


@Module
@InstallIn(SingletonComponent::class)
object SensorModule {

    @Provides
    @Singleton
    @LightSensorQualifier
    fun provideLightSensor(app: Application): MeasurableSensor {
        return LightSensor(app)
    }

    @Provides
    @Singleton
    @AccelerometerQualifier
    fun provideAccelerometerSensor(app: Application): MeasurableSensor {
        return AccelerometerSensor(app)
    }

    @Provides
    @Singleton
    @HeadingSensorQualifier
    fun provideHeadingSensor(app: Application): MeasurableSensor {
        return HeadingSensor(app)
    }

    @Provides
    @Singleton
    @TempSensorQualifier
    fun provideTempSensor(app: Application): MeasurableSensor {
        return TempSensor(app)
    }

}