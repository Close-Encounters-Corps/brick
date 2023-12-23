package org.cec.brick.event

import org.koin.core.component.KoinComponent

sealed class BrickEvent(val name: String) : KoinComponent
