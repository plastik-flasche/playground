#![no_std]
#![no_main]

extern crate atmega_hal;
extern crate panic_halt; // panic handler // hardware abstractions for atmega mcus

use atmega_hal as hal;
use hal::prelude::*;

#[hal::entry]
fn main() -> ! {
    let peripherals = hal::Peripherals::take().unwrap();
    let pins = hal::pins!(peripherals);

    let mut led = pins.pb5.into_output();

    let mut delay = hal::delay::Delay::<hal::clock::MHz16>::new();

    loop {
        led.toggle();
        delay.delay_ms(500u16);
    }
}
