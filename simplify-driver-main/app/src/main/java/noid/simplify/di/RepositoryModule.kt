package noid.simplify.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import noid.simplify.data.MainRepository
import noid.simplify.data.network.NetworkHelper
import noid.simplify.data.network.RetrofitInstance
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMainRepository(
            retrofitInstance: RetrofitInstance,
            networkHelper: NetworkHelper
    ): MainRepository {
        return MainRepository(retrofitInstance, networkHelper)
    }

}