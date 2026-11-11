/*
 * ConnectBot Terminal
 * Copyright 2025 Kenny Root
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.connectbot.terminal

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression tests for runSpanBoundaries: the run edges must always be span
 * boundaries, otherwise a selection intersecting a run drew only the selected
 * slice and the unselected text around it disappeared (hd7 bug).
 */
class RunSpanBoundariesTest {

    private fun spans(
        runStartCol: Int = 0,
        runEndCol: Int = 80,
        selStart: Int = -1,
        selEnd: Int = -2,
        hyper: List<ColSpan> = emptyList(),
    ): List<Int> = runSpanBoundaries(runStartCol, runEndCol, selStart, selEnd, hyper)

    @Test
    fun noSelection_noHyperlinks_drawsWholeRun() {
        assertEquals(listOf(0, 80), spans())
    }

    @Test
    fun emptySelection_drawsWholeRun() {
        assertEquals(listOf(0, 80), spans(selStart = -1, selEnd = -2))
    }

    @Test
    fun selectionInsideRun_splitsIntoThreeSpans() {
        // run 0..80, selection 30..50 -> spans [0,30), [30,51), [51,80)
        assertEquals(listOf(0, 30, 51, 80), spans(selStart = 30, selEnd = 50))
    }

    @Test
    fun selectionCoveringWholeRun_singleSpan() {
        assertEquals(listOf(0, 80), spans(selStart = 0, selEnd = 79))
    }

    @Test
    fun selectionOverflowingLeft_clipsToRunStart() {
        assertEquals(listOf(0, 11, 80), spans(selStart = -5, selEnd = 10))
    }

    @Test
    fun selectionOverflowingRight_clipsToRunEnd() {
        assertEquals(listOf(0, 70, 80), spans(selStart = 70, selEnd = 99))
    }

    @Test
    fun selectionOutsideRun_leavesRunWhole() {
        assertEquals(listOf(0, 80), spans(selStart = 85, selEnd = 90))
        assertEquals(listOf(0, 80), spans(selStart = -10, selEnd = -5))
    }

    @Test
    fun hyperlinkInsideRun_splitsAroundIt() {
        assertEquals(listOf(0, 10, 20, 80), spans(hyper = listOf(ColSpan(10, 20))))
    }

    @Test
    fun selectionAndHyperlinkOverlap_mergedBoundaries() {
        assertEquals(
            listOf(0, 10, 15, 20, 41, 80),
            spans(selStart = 15, selEnd = 40, hyper = listOf(ColSpan(10, 20))),
        )
    }

    @Test
    fun offsetRun_columnsRelativeToRunStart() {
        assertEquals(
            listOf(20, 30, 51, 100),
            spans(runStartCol = 20, runEndCol = 100, selStart = 30, selEnd = 50),
        )
    }
}
