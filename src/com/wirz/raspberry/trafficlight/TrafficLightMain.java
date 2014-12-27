package com.wirz.raspberry.trafficlight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioSyncStateTrigger;

public class TrafficLightMain
{
	private static final Logger slf4jLogger = LoggerFactory.getLogger(TrafficLightMain.class);

	private static final String className = "LedButton";

	public TrafficLightMain()
	{
	}

	private GpioController gpio;
	private GpioPinDigitalOutput ledRed, ledGreen, ledYellow;
	private GpioPinDigitalInput button;

	private enum Operation
	{
		ON, OFF, BLINK, PULSE, TOGGLE
	}

	public static void main(String[] args) throws InterruptedException, IOException
	{
		slf4jLogger.info("Enter {}, main()", className);
		TrafficLightMain myTrafficLight = new TrafficLightMain();
		myTrafficLight.init();
		slf4jLogger.info("Ready to init");

		myTrafficLight.ledHandler(myTrafficLight.ledRed, Operation.OFF, 0);
		myTrafficLight.ledHandler(myTrafficLight.ledYellow, Operation.OFF, 0);
		myTrafficLight.ledHandler(myTrafficLight.ledGreen, Operation.BLINK, 200);
		slf4jLogger.info("Switch ON");
		myTrafficLight.ledHandler(myTrafficLight.ledRed, Operation.ON, 2000);
		Thread.sleep(5000);
		slf4jLogger.info("Switch OFF");
		myTrafficLight.ledHandler(myTrafficLight.ledRed, Operation.OFF, 0);
		Thread.sleep(5000);
		slf4jLogger.info("Switch Pulse 2000ms");
		myTrafficLight.ledHandler(myTrafficLight.ledRed, Operation.PULSE, 2000);
		Thread.sleep(5000);
		slf4jLogger.info("Toggle");
		myTrafficLight.ledHandler(myTrafficLight.ledRed, Operation.TOGGLE, 2000);
		Thread.sleep(5000);
		myTrafficLight.ledHandler(myTrafficLight.ledRed, Operation.ON, 0);
		slf4jLogger.info("LED_RED ON");
		Thread.sleep(5000);
		myTrafficLight.ledHandler(myTrafficLight.ledYellow, Operation.ON, 0);
		slf4jLogger.info("LED_YELLOW ON");
		Thread.sleep(5000);
		myTrafficLight.ledHandler(myTrafficLight.ledGreen, Operation.ON, 0);
		slf4jLogger.info("LED_GREEN ON");
		Thread.sleep(5000);
		myTrafficLight.ledHandler(myTrafficLight.ledRed, Operation.OFF, 0);
		slf4jLogger.info("LED_RED OFF");
		Thread.sleep(5000);
		myTrafficLight.ledHandler(myTrafficLight.ledYellow, Operation.OFF, 0);
		slf4jLogger.info("LED_YELLOW OFF");
		Thread.sleep(5000);
		myTrafficLight.ledHandler(myTrafficLight.ledGreen, Operation.OFF, 0);
		slf4jLogger.info("LED_GREEN OFF");
		Thread.sleep(5000);
		myTrafficLight.addButtonLedSynch(myTrafficLight.ledYellow);
		for (int i = 0; i < 20; i++)
		{
			Thread.sleep(500);
		}
		slf4jLogger.info("End of the Show");
		slf4jLogger.info("Shutdown...");
		myTrafficLight.release();
		slf4jLogger.info("Hardware Released");
		slf4jLogger.info("Leave {}, main()", className);
	}

	private void init()
	{
		gpio = GpioFactory.getInstance();
		ledRed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "LED_RED", PinState.HIGH);
		ledYellow = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, "LED_YELLOW", PinState.HIGH);
		ledGreen = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "LED_GREEN", PinState.HIGH);

		button = gpio.provisionDigitalInputPin(RaspiPin.GPIO_26, // PIN NUMBER
				"BUTTON_26"); // PIN RESISTANCE (optional)
		button.addListener(new ButtonListener());
	}

	private void release()
	{
		ledRed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		ledYellow.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		ledGreen.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();

	}

	private void addButtonLedSynch(GpioPinDigitalOutput led)
	{
		button.addTrigger(new GpioSyncStateTrigger(led));
	}

	private void ledHandler(GpioPinDigitalOutput led, Operation COMMAND, long duration)
	{
		switch (COMMAND)
		{

		case ON:
		{
			led.high();
			break;
		}

		case OFF:
		{
			led.low();
			break;
		}

		case PULSE:
		{
			led.pulse(duration);
			break;
		}
		case TOGGLE:
		{
			led.toggle();
			break;
		}
		case BLINK:
		{
			led.blink(200, 5000);
			break;
		}
		default:
			break;
		}
	}

	public class ButtonListener implements GpioPinListenerDigital
	{
		@Override
		public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
		{
			// display pin state on console
			slf4jLogger.info(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
		}
	}

}
