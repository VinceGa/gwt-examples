package com.gawkat.flashcard.client.rpc;

import com.gawkat.flashcard.client.card.MathData;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("rpcservice")
public interface RpcService extends RemoteService {
  
  public MathData getMathData(MathData mathData);
  
}
