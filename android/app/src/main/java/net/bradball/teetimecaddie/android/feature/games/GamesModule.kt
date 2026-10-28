package net.bradball.teetimecaddie.android.feature.games

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.bradball.teetimecaddie.TeeTimeCaddieSdk
import net.bradball.teetimecaddie.android.feature.games.contacts.AndroidContactsPermission
import net.bradball.teetimecaddie.android.feature.games.contacts.AndroidDeviceContactsReader
import net.bradball.teetimecaddie.android.feature.games.contacts.ContactsPermission
import net.bradball.teetimecaddie.features.games.ContactsRepository
import net.bradball.teetimecaddie.features.games.GamesRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class GamesModule {

    @Provides
    @Singleton
    fun provideGamesRepository(): GamesRepository {
        return TeeTimeCaddieSdk.getInstance().gamesRepository
    }

    @Provides
    @Singleton
    fun provideContactsRepository(reader: AndroidDeviceContactsReader): ContactsRepository {
        return TeeTimeCaddieSdk.getInstance().contactsRepository(reader)
    }

    @Provides
    fun provideContactsPermission(permission: AndroidContactsPermission): ContactsPermission = permission
}
