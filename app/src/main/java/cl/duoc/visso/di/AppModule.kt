package cl.duoc.visso.di

import android.content.Context
import cl.duoc.visso.data.remote.ApiService
import cl.duoc.visso.data.remote.RetrofitClient
import cl.duoc.visso.data.repository.*
import cl.duoc.visso.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return RetrofitClient.apiService
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService): AuthRepository {
        return AuthRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideProductoRepository(apiService: ApiService): ProductoRepository {
        return ProductoRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideCarritoRepository(apiService: ApiService): CarritoRepository {
        return CarritoRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(apiService: ApiService): UsuarioRepository {
        return UsuarioRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideCotizacionRepository(apiService: ApiService): CotizacionRepository {
        return CotizacionRepository(apiService)
    }
}