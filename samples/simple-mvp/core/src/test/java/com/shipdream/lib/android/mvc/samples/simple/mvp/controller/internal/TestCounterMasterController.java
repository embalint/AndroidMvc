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

package com.shipdream.lib.android.mvc.samples.simple.mvp.controller.internal;

import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.MvcComponent;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.TestUtil;
import com.shipdream.lib.android.mvc.samples.simple.mvp.controller.CounterDetailController;
import com.shipdream.lib.android.mvc.samples.simple.mvp.controller.CounterMasterController;
import com.shipdream.lib.android.mvc.samples.simple.mvp.dto.IpPayload;
import com.shipdream.lib.android.mvc.samples.simple.mvp.factory.ServiceFactory;
import com.shipdream.lib.android.mvc.samples.simple.mvp.http.IpService;
import com.shipdream.lib.android.mvc.samples.simple.mvp.manager.CounterManager;
import com.shipdream.lib.android.mvc.samples.simple.mvp.service.ResourceService;
import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.ProvideException;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Response;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCounterMasterController extends BaseTest {
    @Inject
    private CounterManager counterManager;

    @Inject
    private NavigationManager navigationManager;

    private CounterMasterController controller;

    private ResourceService resourceServiceMock;

    @Override
    protected void prepareGraph(MvcComponent overriddingComponent) throws Exception {
        super.prepareGraph(overriddingComponent);
        overriddingComponent.register(new Provider<ResourceService>(ResourceService.class) {
            @Override
            protected ResourceService createInstance() throws ProvideException {
                resourceServiceMock = mock(ResourceService.class);
                return resourceServiceMock;
            }
        });
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        controller = new CounterMasterController();
        Mvc.graph().inject(controller);
        controller.onCreated();
    }

    @Test
    public void increment_should_post_counter_update_event_with_incremented_value() {
        //1. Prepare view
        CounterMasterController.View view = mock(CounterMasterController.View.class);
        TestUtil.assignControllerView(controller, view);

        //mock controller model for count value
        int value = new Random().nextInt();
        CounterManager.Model counterModel = new CounterManager.Model();
        counterModel.setCount(value);
        //Mock the model of manager
        counterManager.bindModel(this, counterModel);

        //2. Act
        controller.increment(this);

        //3. Verify
        verify(view, times(1)).update();
        Assert.assertEquals(String.valueOf(value + 1), controller.getModel().getCount());

    }

    @Test
    public void should_navigate_correctly() {
        //Act: navigate to MasterScreen
        navigationManager.navigate(this).to(CounterMasterController.class);

        //Verify: location should be changed to MasterScreen
        Assert.assertEquals(CounterMasterController.class.getName(),
                navigationManager.getModel().getCurrentLocation().getLocationId());

        //Act: navigate to DetailScreen
        controller.goToDetailScreen(this);

        //Verify: Current location should be at the view paired with CounterDetailController
        Assert.assertEquals(CounterDetailController.class.getName(),
                navigationManager.getModel().getCurrentLocation().getLocationId());
    }

    @Test
    public void should_update_view_with_correct_ip_and_show_and_dismiss_progress_bar() throws Exception {
        //Prepare view
        CounterMasterController.View view = mock(CounterMasterController.View.class);
        TestUtil.assignControllerView(controller, view);

        final String fakeIpResult = "abc.123.456.xyz";

        MvcComponent component = new MvcComponent("ServiceComponent");
        component.register(new Object(){
            /**
             * Prepare http service
             * @return
             * @throws IOException
             */
            @Provides
            public ServiceFactory serviceFactory() throws IOException {
                IpPayload payload = mock(IpPayload.class);
                when(payload.getIp()).thenReturn(fakeIpResult);

                Call<IpPayload> call = mock(Call.class);
                when(call.execute()).thenReturn(Response.success(payload));

                IpService ipService = mock(IpService.class);
                when(ipService.getIp(anyString())).thenReturn(call);

                final ServiceFactory serviceFactory = mock(ServiceFactory.class);
                when(serviceFactory.createService(IpService.class)).thenReturn(ipService);
                return serviceFactory;
            }
        });

        boolean overrideProvider = true;
        Mvc.graph().getRootComponent().attach(component, overrideProvider);
        Mvc.graph().inject(controller);

        controller.refreshIp();

        //Verify
        //Showed loading progress
        verify(view).showProgress();

        //Dismissed loading progress
        verify(view).hideProgress();

        //Updated view's text view by the given fake ip result
        verify(view).updateIpValue(fakeIpResult);

        //Should not show error message
        verify(view, times(0)).showErrorMessageToFetchIp();
    }
}
