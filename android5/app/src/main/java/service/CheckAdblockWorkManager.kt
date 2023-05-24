/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package service

import analytics.FdLog
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.adshield.R

class CheckAdblockWorkManager constructor(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    override fun doWork(): Result {
        FdLog.d(TAG, "doWork()")
        val isAdblockWork = CheckAdblockWorkService.isAdblockWork()
        MonitorService.setInfo(if (!isAdblockWork) R.string.str_stop_working_push_info else 0)
        return Result.success()
    }

    companion object {
        private const val TAG = "CheckAdblockWorkManager"
    }
}