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

@SuppressWarnings("unused")
public class TrafficLightMain
{
	private static final Logger slf4jLogger = LoggerFactory.getLogger(TrafficLightMain.class);

	private static final String className = "TrafficLightMain";

	private GpioController gpio;
	private GpioPinDigitalOutput ledRed, ledGreen, ledYellow;
	private GpioPinDigitalInput button;
	private TrafficLightState myTrafficLightState;
	private boolean keyDetected;
	private int timeOut = 1000;
	boolean running = true;
	boolean debug = true;

	private enum Operation
	{
		ON, OFF, BLINK, PULSE, TOGGLE
	}

	private enum TrafficLightState
	{
		IDLE, IDLE_KEY_PRESSED, IDLE_KEY_ACCEPTED, ACTIVE
	}

	public TrafficLightMain()
	{
		myTrafficLightState = TrafficLightState.IDLE;
	}

	public static void main(String[] args) throws InterruptedException, IOException
	{
		TrafficLightMain myTrafficLight;
		slf4jLogger.info("Enter {}, main()", className);
		myTrafficLight = new TrafficLightMain();
		myTrafficLight.init();
		slf4jLogger.info("TrafficLightMain is initiated");
		myTrafficLight.run();
		myTrafficLight.release();
		slf4jLogger.info("Leave {}, main()", className);
	}

	private void init()
	{
		gpio = GpioFactory.getInstance();
		ledRed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "LED_RED", PinState.LOW);
		ledYellow = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, "LED_YELLOW", PinState.LOW);
		ledGreen = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "LED_GREEN", PinState.LOW);
		
		button = gpio.provisionDigitalInputPin(RaspiPin.GPIO_26, // PIN NUMBER
				"BUTTON_26"); // PIN RESISTANCE (optional)
		button.addListener(new ButtonListener());
	}

	private void release()
	{
		slf4jLogger.info("Shutting down...");
		ledRed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		ledYellow.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		ledGreen.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		slf4jLogger.info("Hardware Released");

	}

	private void run() throws InterruptedException
	{
		this.blinkAllLeds();
		this.setLedIdleState();
		this.stateMachine();
	}

	private void stateMachine() throws InterruptedException
	{
		while (running)
		{
			if (debug)
			{
				slf4jLogger.debug("In-State: {}", myTrafficLightState);
			}
			switch (myTrafficLightState)
			{
			case IDLE:
				timeOut = 1000;
				if (debug)
				{
					slf4jLogger.debug("Random-Timeout: {}", timeOut);
				}
				Thread.sleep(timeOut);
				if (keyDetected)
				{
					myTrafficLightState = TrafficLightState.IDLE_KEY_PRESSED;
				}
				break;
			case IDLE_KEY_PRESSED:
				timeOut = getRandom(1000, 2000);
				if (debug)
				{
					slf4jLogger.debug("Random-Timeout: {}", timeOut);
				}
				Thread.sleep(timeOut);
				this.ledHandler(this.ledYellow, Operation.ON, 0);
				myTrafficLightState = TrafficLightState.IDLE_KEY_ACCEPTED;
				break;
			case IDLE_KEY_ACCEPTED:
				timeOut = getRandom(2000, 3000);
				if (debug)
				{
					slf4jLogger.debug("Random-Timeout: {}", timeOut);
				}
				Thread.sleep(timeOut);
				this.ledHandler(this.ledYellow, Operation.OFF, 0);
				this.ledHandler(this.ledRed, Operation.OFF, 0);
				this.ledHandler(this.ledGreen, Operation.ON, 0);
				keyDetected = false;
				myTrafficLightState = TrafficLightState.ACTIVE;
				break;
			case ACTIVE:
				timeOut = getRandom(4000, 10000);
				if (debug)
				{
					slf4jLogger.debug("Random-Timeout: {}", timeOut);
				}
				Thread.sleep(timeOut);
				this.ledHandler(this.ledYellow, Operation.OFF, 0);
				this.ledHandler(this.ledGreen, Operation.OFF, 0);
				this.ledHandler(this.ledRed, Operation.ON, 0);
				myTrafficLightState = TrafficLightState.IDLE;
				break;
			default:
				break;

			}
			if (debug)
			{
				slf4jLogger.debug("Out-State: {}", myTrafficLightState);
			}
		}
		slf4jLogger.debug("Prepare shutdown...");
		this.ledHandler(this.ledYellow, Operation.OFF, 0);
		this.ledHandler(this.ledGreen, Operation.OFF, 0);
		this.ledHandler(this.ledRed, Operation.OFF, 0);
		Thread.sleep(500);
		this.ledHandler(this.ledYellow, Operation.BLINK, 200);
		this.ledHandler(this.ledGreen, Operation.BLINK, 200);
		this.ledHandler(this.ledRed, Operation.BLINK, 2000);
		Thread.sleep(3000);
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

	private int getRandom(int min, int max)
	{
		if (min > max)
		{
			slf4jLogger.warn("Condition: max  {} > min {} not fullfilled; will change max<>min", max, min);
			int temp = min;
			min = max;
			max = temp;
		}
		return timeOut = min + (int) Math.floor((Math.random() * (max - min)) + 1);
	}

	private class ButtonListener implements GpioPinListenerDigital
	{
		@Override
		public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
		{
			// display pin state on console
			slf4jLogger.info(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
			if ((event.getState() == PinState.LOW) && (myTrafficLightState == TrafficLightState.IDLE))
			{
				keyDetected = true;
				slf4jLogger.info("keyDetected = true ");
			} else if (myTrafficLightState == TrafficLightState.ACTIVE)
			{
				running = false;
				slf4jLogger.info("running = false");
			}

		}
	}
	
	private void blinkAllLeds() throws InterruptedException
	{
		this.ledHandler(this.ledYellow, Operation.OFF, 0);
		this.ledHandler(this.ledGreen, Operation.OFF, 0);
		this.ledHandler(this.ledRed, Operation.OFF, 0);
		Thread.sleep(100);
		this.ledHandler(this.ledYellow, Operation.BLINK, 200);
		this.ledHandler(this.ledGreen, Operation.BLINK, 200);
		this.ledHandler(this.ledRed, Operation.BLINK, 2000);
		Thread.sleep(5000);
	}
	
	private void setLedIdleState()
	{
		this.ledHandler(this.ledYellow, Operation.OFF, 0);
		this.ledHandler(this.ledGreen, Operation.OFF, 0);
		this.ledHandler(this.ledRed, Operation.ON, 0);
	}
}
