package net.bradball.teetimecaddie.features.games.state

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.NewInvitee

// Swift can't see Kotlin's default arguments, so `copy` there takes every field. These make a new
// state with one field changed, without the caller repeating the rest. Kotlin callers use `copy`.

fun NewGameUiState.withCourse(course: String): NewGameUiState = copy(course = course)

fun NewGameUiState.withDate(date: LocalDate): NewGameUiState = copy(date = date)

fun NewGameUiState.withTime(time: LocalTime): NewGameUiState = copy(time = time)

fun NewGameUiState.withSlots(slots: Int): NewGameUiState = copy(slots = slots)

fun NewGameUiState.withInvitees(invitees: List<NewInvitee>): NewGameUiState = copy(invitees = invitees)

fun NewGameUiState.withNoInviteeConfirm(showNoInviteeConfirm: Boolean): NewGameUiState =
    copy(showNoInviteeConfirm = showNoInviteeConfirm)

fun NewGameUiState.withSubmitting(isSubmitting: Boolean): NewGameUiState = copy(isSubmitting = isSubmitting)
