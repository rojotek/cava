/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.consensys.cava.scuttlebutt.rpc;

import net.consensys.cava.bytes.Bytes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encoder responsible for encoding requests.
 * <p>
 * This encoder is stateful as it maintains a counter to provide different request ids over time.
 */
public final class RPCCodec {

  static final AtomicInteger counter = new AtomicInteger(1);

  private static int nextRequestNumber() {
    int requestNumber = counter.getAndIncrement();
    if (requestNumber < 1) {
      counter.set(1);
      return 1;
    }
    return requestNumber;
  }

  /**
   * Encode a message as a RPC request.
   * 
   * @param body the body to encode as a RPC request
   * @param flags the flags of the RPC request
   * @return the message encoded as a RPC request
   */
  public static Bytes encodeRequest(String body, RPCFlag... flags) {
    return encodeRequest(Bytes.wrap(body.getBytes(StandardCharsets.UTF_8)), nextRequestNumber(), flags);
  }

  /**
   * Encode a message as a RPC request.
   * 
   * @param body the body to encode as a RPC request
   * @param flags the flags of the RPC request
   * @return the message encoded as a RPC request
   */
  public static Bytes encodeRequest(Bytes body, RPCFlag... flags) {
    return encodeRequest(body, nextRequestNumber(), flags);
  }

  /**
   * Encode a message as a RPC request.
   * 
   * @param body the body to encode as a RPC request
   * @param requestNumber the number of the request. Must be equal or greater than one.
   * @param flags the flags of the RPC request
   * @return the message encoded as a RPC request
   */
  public static Bytes encodeRequest(Bytes body, int requestNumber, RPCFlag... flags) {
    if (requestNumber < 1) {
      throw new IllegalArgumentException("Invalid request number");
    }
    byte encodedFlags = 0;
    for (RPCFlag flag : flags) {
      encodedFlags = flag.apply(encodedFlags);
    }
    return Bytes.concatenate(
        Bytes.of(encodedFlags),
        Bytes.ofUnsignedInt(body.size()),
        Bytes.ofUnsignedInt(requestNumber),
        body);
  }

  /**
   * Encode a message as a response to a RPC request.
   * 
   * @param body the body to encode as the body of the response
   * @param requestNumber the request of the number. Must be equal or greater than one.
   * @param flagByte the flags of the RPC response encoded as a byte
   * @return the response encoded as a RPC response
   */
  public static Bytes encodeResponse(Bytes body, int requestNumber, byte flagByte) {
    if (requestNumber < 1) {
      throw new IllegalArgumentException("Invalid request number");
    }
    return Bytes.concatenate(
        Bytes.of(flagByte),
        Bytes.ofUnsignedInt(body.size()),
        Bytes.wrap(ByteBuffer.allocate(4).putInt(-requestNumber).array()),
        body);
  }

  /**
   * Encode a message as a response to a RPC request.
   * 
   * @param body the body to encode as the body of the response
   * @param requestNumber the request of the number. Must be equal or greater than one.
   * @param flagByte the flags of the RPC response encoded as a byte
   * @param flags the flags of the RPC request
   * @return the response encoded as a RPC response
   */
  public static Bytes encodeResponse(Bytes body, int requestNumber, byte flagByte, RPCFlag... flags) {
    for (RPCFlag flag : flags) {
      flagByte = flag.apply(flagByte);
    }
    return encodeResponse(body, requestNumber, flagByte);
  }

  /**
   * Encode a message as a response to a RPC request.
   * 
   * @param body the body to encode as the body of the response
   * @param requestNumber the request of the number. Must be equal or greater than one.
   * @param flags the flags of the RPC request
   * @return the response encoded as a RPC response
   */
  public static Bytes encodeResponse(Bytes body, int requestNumber, RPCFlag... flags) {
    return encodeResponse(body, requestNumber, (byte) 0, flags);
  }
}
