trafficlight
============

My base prj for Raspberry Pi B+.

The project implements the trafficlight at the crossing Amthausquai - Froburgstrasse at Olten, Switzerland.
The project runs in a raspberry pi B+ environment. After a init phase (all leds blinking) the statemachine starts in IDLE state (LED_RED).
By pressing the button, the LED 'Button accepted' lights (LED_GREEN) for a random time. After, the light changes to green (LED_GREEN) for an other random time, and then back to red, waiting for a next "push button". Pushing the button during a green phase stops the process.

Implemented in java using the pi4j library. It is my first raspi project, developed under Windows/Eclipse with remot debug/run
environment.

LED_RED     gpio_29
LED_YELLOW  gpio_28
LED_GREEN   gpio_27
Button      gpio_26

Thanks Danu for the great rasi!
