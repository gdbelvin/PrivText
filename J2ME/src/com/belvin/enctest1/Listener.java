/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.belvin.enctest1;

import javax.wireless.messaging.TextMessage;

/**
 * This class supports polymorphism for the reciever text messaging engine
 * @author Administrator
 */
public interface Listener {

    public void processSMS(TextMessage theMessage);

}
