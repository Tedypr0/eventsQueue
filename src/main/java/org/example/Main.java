package org.example;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        SideStuff stuff = new SideStuff();
        stuff.eventReferencesCreationAndThreadStartup();
        stuff.eventCreation();
    }
}