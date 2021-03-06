/*
 * Copyright 2016 Kejun Xia
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

package com.shipdream.lib.android.mvc;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestFragmentController extends BaseTest {
    @Test
    public void should_run_all_life_cycle_calls_without_exception() {
        FragmentController controller = new FragmentController() {
            @Override
            public Class modelType() {
                return null;
            }
        };

        Assert.assertFalse(controller.onBackButtonPressed());
        controller.currentOrientation();
        controller.onOrientationChanged(Orientation.LANDSCAPE, Orientation.PORTRAIT);
        controller.onResume();
        controller.onPause();
        controller.onPopAway();
        controller.onPoppedOutToFront();
        controller.onPushToBackStack();
        controller.onReturnForeground();

        UiView view = mock(UiView.class);
        controller.view = view;
        controller.onViewReady(new Reason());

        verify(view).update();
    }
}
