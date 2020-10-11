/* 
 * BSD 2-Clause License
 * 
 * Copyright (c) 2020, MasterKenth
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package com.masterkenth;

import java.util.concurrent.CompletableFuture;
import java.util.Collection;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.client.game.ItemVariationMapping;

@Slf4j
public class RarityChecker
{
  public float CheckRarityEvent(String eventName, int itemId)
  {
    String lowerName = eventName.toLowerCase();
    Map<Integer, Float> table = EventRarity.EVENT_TABLE_MAPPING.getOrDefault(lowerName, null);
    if (table != null)
    {
      int mapId = ItemVariationMapping.map(itemId);
      Collection<Integer> idVariations = ItemVariationMapping.getVariations(mapId);

      for (Integer id : idVariations)
      {
        if (table.containsKey(id))
        {
          return table.get(id);
        }
      }

      return -1f;
    }
    else
    {
      log.warn("No event table for '" + eventName + "'");
    }

    return -1f;
  }

  public CompletableFuture<Float> CheckRarityNPC(int npcId, int itemId)
  {
    CompletableFuture<Float> f = new CompletableFuture<>();

    ApiTool.getInstance().getNPC(npcId).thenAccept(npcJson ->
    {
      try
      {
        JSONArray drops = npcJson.getJSONArray("drops");
        int mapId = ItemVariationMapping.map(itemId);
        Collection<Integer> idVariations = ItemVariationMapping.getVariations(mapId);

        for (int i = 0; i < drops.length(); i++)
        {
          JSONObject drop = drops.getJSONObject(i);
          int dropId = drop.getInt("id");
          if (idVariations.contains(dropId))
          {
            float rarity = drop.getFloat("rarity");
            f.complete(rarity);
            return;
          }
        }
        // No entry for item, default to 100% drop
        f.complete(1.0f);
      }
      catch (Exception e)
      {
        f.completeExceptionally(e);
      }
    });

    return f;
  }
}
