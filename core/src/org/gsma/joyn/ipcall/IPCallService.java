/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.gsma.joyn.ipcall;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gsma.joyn.JoynContactFormatException;
import org.gsma.joyn.JoynService;
import org.gsma.joyn.JoynServiceException;
import org.gsma.joyn.JoynServiceListener;
import org.gsma.joyn.JoynServiceNotAvailableException;
import org.gsma.joyn.JoynServiceRegistrationListener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * This class offers the main entry point to initiate IP calls. Several
 * applications may connect/disconnect to the API.
 * 
 * The parameter contact in the API supports the following formats:
 * MSISDN in national or international format, SIP address, SIP-URI
 * or Tel-URI.
 * 
 * @author Jean-Marc AUFFRET
 */
public class IPCallService extends JoynService {
	/**
	 * API
	 */
	private IIPCallService api = null;
	
    /**
     * Constructor
     * 
     * @param ctx Application context
     * @param listener Service listener
     */
    public IPCallService(Context ctx, JoynServiceListener listener) {
    	super(ctx, listener);
    }

    /**
     * Connects to the API
     */
    public void connect() {
    	ctx.bindService(new Intent(IIPCallService.class.getName()), apiConnection, 0);
    }
    
    /**
     * Disconnects from the API
     */
    public void disconnect() {
    	try {
    		ctx.unbindService(apiConnection);
        } catch(IllegalArgumentException e) {
        	// Nothing to do
        }
    }
    
    /**
     * Returns true if connected to the service, else returns false
     * 
	 * @return Returns true if connected else returns false
     */
    public boolean isServiceConnected() {
    	return (api != null);
    }

    /**
	 * Service connection
	 */
	private ServiceConnection apiConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	api = IIPCallService.Stub.asInterface(service);
        	if (serviceListener != null) {
        		serviceListener.onServiceConnected();
        	}
        }

        public void onServiceDisconnected(ComponentName className) {
        	api = null;
        	if (serviceListener != null) {
        		serviceListener.onServiceDisconnected(JoynService.Error.CONNECTION_LOST);
        	}
        }
    };
    
    /**
     * Returns true if the service is registered to the platform, else returns false
     * 
	 * @return Returns true if registered else returns false
     * @throws JoynServiceException
     */
    public boolean isServiceRegistered() throws JoynServiceException {
		if (api != null) {
			try {
				return api.isServiceRegistered();
			} catch(Exception e) {
				throw new JoynServiceException(e.getMessage());
			}
		} else {
			throw new JoynServiceNotAvailableException();
		}
    }

	/**
	 * Registers a listener on service registration events
	 * 
	 * @param listener Service registration listener
     * @throws JoynServiceException
	 */
	public void addServiceRegistrationListener(JoynServiceRegistrationListener listener) throws JoynServiceException {
		if (api != null) {
			try {
				api.addServiceRegistrationListener(listener);
			} catch(Exception e) {
				throw new JoynServiceException(e.getMessage());
			}
		} else {
			throw new JoynServiceNotAvailableException();
		}
	}
	
	/**
	 * Unregisters a listener on service registration events
	 * 
	 * @param listener Service registration listener
     * @throws JoynServiceException
	 */
	public void removeServiceRegistrationListener(JoynServiceRegistrationListener listener) throws JoynServiceException {
		if (api != null) {
			try {
				api.removeServiceRegistrationListener(listener);
			} catch(Exception e) {
				throw new JoynServiceException(e.getMessage());
			}
		} else {
			throw new JoynServiceNotAvailableException();
		}
	}
	
    /**
     * Returns the configuration of IP call service
     * 
     * @return Configuration
     * @throws JoynServiceException
     */
    public IPCallServiceConfiguration getConfiguration() throws JoynServiceException {
		if (api != null) {
			try {
				return api.getConfiguration();
			} catch(Exception e) {
				throw new JoynServiceException(e.getMessage());
			}
		} else {
			throw new JoynServiceNotAvailableException();
		}
	}

    /**
     * Initiates an IP call with a contact. The parameter contact supports the following
     * formats: MSISDN in national or international format, SIP address, SIP-URI or
     * el-URI. If the format of the contact is not supported an exception is thrown.
     * 
     * @param contact Contact
     * @param player IP call player
     * @param renderer IP call renderer
     * @param listener IP call event listener
     * @return IP call
     * @throws JoynServiceException
	 * @throws JoynContactFormatException
     */
    public IPCall initiateCall(String contact, IPCallPlayer player, IPCallRenderer renderer, IPCallListener listener) throws JoynServiceException, JoynContactFormatException {
		if (api != null) {
			try {
				IIPCall callIntf = api.initiateCall(contact, player, renderer, listener);
				if (callIntf != null) {
					return new IPCall(callIntf);
				} else {
					return null;
				}
			} catch(Exception e) {
				throw new JoynServiceException(e.getMessage());
			}
		} else {
			throw new JoynServiceNotAvailableException();
		}
    }    
    
    /**
     * Returns the list of IP calls in progress
     * 
     * @return List of IP calls
     * @throws JoynServiceException
     */
    public Set<IPCall> getIPCalls() throws JoynServiceException {
		if (api != null) {
			try {
	    		Set<IPCall> result = new HashSet<IPCall>();
				List<IBinder> vshList = api.getIPCalls();
				for (IBinder binder : vshList) {
					IPCall call = new IPCall(IIPCall.Stub.asInterface(binder));
					result.add(call);
				}
				return result;
			} catch(Exception e) {
				throw new JoynServiceException(e.getMessage());
			}
		} else {
			throw new JoynServiceNotAvailableException();
		}
    }    

    /**
     * Returns a current IP call from its unique ID
     * 
     * @param callId Call ID
     * @return IP call or null if not found
     * @throws JoynServiceException
     */
    public IPCall getIPCall(String callId) throws JoynServiceException {
		if (api != null) {
			try {
				IIPCall callIntf = api.getIPCall(callId);
				if (callIntf != null) {
					return new IPCall(callIntf);
				} else {
					return null;
				}
			} catch(Exception e) {
				throw new JoynServiceException(e.getMessage());
			}
		} else {
			throw new JoynServiceNotAvailableException();
		}
    }    
    
    /**
     * Returns a current IP call from its invitation Intent
     * 
     * @param intent Invitation intent
     * @return IP call or null if not found
     * @throws JoynServiceException
     */
    public IPCall getIPCallFor(Intent intent) throws JoynServiceException {
		if (api != null) {
			try {
				String callId = intent.getStringExtra(IPCallIntent.EXTRA_CALL_ID);
				if (callId != null) {
					return getIPCall(callId);
				} else {
					return null;
				}
			} catch(Exception e) {
				throw new JoynServiceException(e.getMessage());
			}
		} else {
			throw new JoynServiceNotAvailableException();
		}
    }     
    
    /**
	 * Registers a new IP call invitation listener
	 * 
	 * @param listener New IP call listener
	 * @throws JoynServiceException
	 */
	public void addNewIPCallListener(NewIPCallListener listener) throws JoynServiceException {
		if (api != null) {
			try {
				api.addNewIPCallListener(listener);
			} catch(Exception e) {
				throw new JoynServiceException(e.getMessage());
			}
		} else {
			throw new JoynServiceNotAvailableException();
		}
	}

	/**
	 * Unregisters a new IP call invitation listener
	 * 
	 * @param listener New IP call listener
	 * @throws JoynServiceException
	 */
	public void removeNewIPCallListener(NewIPCallListener listener) throws JoynServiceException {
		if (api != null) {
			try {
				api.removeNewIPCallListener(listener);
			} catch(Exception e) {
				throw new JoynServiceException(e.getMessage());
			}
		} else {
			throw new JoynServiceNotAvailableException();
		}
	}
}
