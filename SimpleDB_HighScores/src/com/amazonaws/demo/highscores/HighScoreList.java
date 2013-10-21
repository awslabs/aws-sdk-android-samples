/*
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.demo.highscores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;

// ========================================================
// This article provides more details on SimpleDB Queries.
// http://aws.amazon.com/articles/1231
// ========================================================


/*
 * This class provides all the functionality for the High Scores list.
 *
 * The class uses SimpleDB to store individuals Items in a Domain.  
 * Each Item represents a player and their score.
 */
public class HighScoreList {	
	public static final int PLAYER_SORT = 1;
	public static final int SCORE_SORT  = 2;
	public static final int NO_SORT     = 0;
	
	private static final String HIGH_SCORES_DOMAIN = "HighScores";
	
	private static final String PLAYER_ATTRIBUTE = "player";
	private static final String SCORE_ATTRIBUTE = "score";
	
	private static final String PLAYER_SORT_QUERY = "select player, score from HighScores where player > '' order by player asc";
	private static final String SCORE_SORT_QUERY = "select player, score from HighScores where score >= '0' order by score desc";
	private static final String NO_SORT_QUERY = "select player, score from HighScores";
	
	private static final String COUNT_QUERY = "select count(*) from HighScores";
		
	protected AmazonSimpleDBClient sdbClient;
	protected String nextToken;
	protected int sortMethod;
	protected int count;
	
	public HighScoreList() {
		this( NO_SORT );
	}
		
	public HighScoreList( int sortMethod ) {
        // Initial the SimpleDB Client.
		AWSCredentials credentials = new BasicAWSCredentials( Constants.ACCESS_KEY_ID, Constants.SECRET_KEY );
        this.sdbClient = new AmazonSimpleDBClient( credentials); 
        this.sdbClient.setRegion(Region.getRegion(Regions.US_WEST_2));
		this.nextToken = null;
		this.sortMethod = sortMethod;
		this.count = -1;
	}
	
    /*
     * Gets the item from the High Scores domain with the item name equal to 'thePlayer'.
     */
	public HighScore getPlayer( String player ) {
		GetAttributesRequest gar = new GetAttributesRequest( HIGH_SCORES_DOMAIN, player );
		GetAttributesResult response = this.sdbClient.getAttributes(gar);
		
		String playerName = this.getStringValueForAttributeFromList( PLAYER_ATTRIBUTE, response.getAttributes() );				
		int score = this.getIntValueForAttributeFromList( SCORE_ATTRIBUTE, response.getAttributes() );
		
		return new HighScore( playerName, score );		
	}
	
    /*
     * Method returns the number of items in the High Scores Domain.
     */
	public int getCount() {
		if ( count < 0 ) {
			SelectRequest selectRequest = new SelectRequest( COUNT_QUERY ).withConsistentRead( true );
			List<Item> items = this.sdbClient.select( selectRequest ).getItems();	
			
			Item countItem = (Item)items.get(0);
			Attribute countAttribute = (Attribute)(((com.amazonaws.services.simpledb.model.Item) countItem).getAttributes().get(0));
			this.count = Integer.parseInt( countAttribute.getValue() );
		}

		return this.count;
	}
	
    /*
     * Using the pre-defined query, extracts items from the domain in a determined order using the 'select' operation.
     */
	public synchronized List<HighScore> getHighScores() {
		String query = null;
		switch ( this.sortMethod ) {
			case PLAYER_SORT: query = PLAYER_SORT_QUERY; break;
			case SCORE_SORT: query = SCORE_SORT_QUERY; break;
			default: query = NO_SORT_QUERY; break;
		}
		
		SelectRequest selectRequest = new SelectRequest( query ).withConsistentRead( true );
		selectRequest.setNextToken( this.nextToken );
		
		SelectResult response = this.sdbClient.select( selectRequest );
		this.nextToken = response.getNextToken();
		
		return this.convertItemListToHighScoreList( response.getItems() );	
	}
	
    /*
     * If a 'nextToken' was returned on the previous query execution, use the next token to get the next batch of items.
     */
	@SuppressWarnings("unchecked")
	public synchronized List<HighScore> getNextPageOfScores() {
		if ( this.nextToken == null ) {
			return Collections.EMPTY_LIST;
		}
		else {
			return this.getHighScores();
		}
	}
	
    /*
     * Creates a new item and adds it to the HighScores domain.
     */
	public void addHighScore( HighScore score ) {
		String paddedScore = SimpleDBUtils.encodeZeroPadding( score.getScore(), 10 );
		
		ReplaceableAttribute playerAttribute = new ReplaceableAttribute( PLAYER_ATTRIBUTE, score.getPlayer(), Boolean.TRUE );
		ReplaceableAttribute scoreAttribute = new ReplaceableAttribute( SCORE_ATTRIBUTE, paddedScore, Boolean.TRUE );
		
		List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>(2);
		attrs.add( playerAttribute );
		attrs.add( scoreAttribute );
		
		PutAttributesRequest par = new PutAttributesRequest( HIGH_SCORES_DOMAIN, score.getPlayer(), attrs);		
		try {
			this.sdbClient.putAttributes( par );
		}
		catch ( Exception exception ) {
			System.out.println( "EXCEPTION = " + exception );
		}
	}
	
    /*
     * Removes the item from the HighScores domain.
     * The item removes is the item whose 'player' matches the theHighScore submitted.
     */
	public void removeHighScore( HighScore score ) {
		DeleteAttributesRequest dar = new DeleteAttributesRequest( HIGH_SCORES_DOMAIN, score.getPlayer() );
		this.sdbClient.deleteAttributes( dar );
		this.count = -1; // To force count refresh.		
	}
	
    /*
     * Creates the HighScore domain.
     */
	public void createHighScoresDomain() {
		CreateDomainRequest cdr = new CreateDomainRequest( HIGH_SCORES_DOMAIN );
		this.sdbClient.createDomain( cdr );
	}
		
    /*
     * Deletes the HighScore domain.
     */
	public void clearHighScores() {
		DeleteDomainRequest ddr = new DeleteDomainRequest( HIGH_SCORES_DOMAIN );
		this.sdbClient.deleteDomain(ddr);
	}
		
    /*
     * Converts an array of Items into an array of HighScore objects.
     */
	protected List<HighScore> convertItemListToHighScoreList( List<Item> items ) {
		List<HighScore> scores = new ArrayList<HighScore>( items.size() );
		for ( Item item : items ) {
			scores.add( this.convertItemToHighScore( item ) );
		}
		
		return scores;
	}
	
    /*
     * Converts a single SimpleDB Item into a HighScore object.
     */
	protected HighScore convertItemToHighScore( Item item ) {
		return new HighScore( this.getPlayerForItem( item ), this.getScoreForItem( item ) );
	}	
	
    /*
     * Extracts the 'player' attribute from the SimpleDB Item.
     */
	protected String getPlayerForItem( Item item ) {
		return this.getStringValueForAttributeFromList( PLAYER_ATTRIBUTE, item.getAttributes() );
	}

    /*
     * Extracts the 'score' attribute from the SimpleDB Item.
     */
	protected int getScoreForItem( Item item ) {
		return this.getIntValueForAttributeFromList( SCORE_ATTRIBUTE, item.getAttributes() );
	}
	
    /*
     * Extracts the value for the given attribute from the list of attributes.
     * Extracted value is returned as a String.
     */
	protected String getStringValueForAttributeFromList( String attributeName, List<Attribute> attributes ) {
		for ( Attribute attribute : attributes ) {
			if ( attribute.getName().equals( attributeName ) ) {
				return attribute.getValue();
			}
		}
		
		return "";		
	}
	
    /*
     * Extracts the value for the given attribute from the list of attributes.
     * Extracted value is returned as an int.
     */
	protected int getIntValueForAttributeFromList( String attributeName, List<Attribute> attributes ) {
		for ( Attribute attribute : attributes ) {
			if ( attribute.getName().equals( attributeName ) ) {
				return Integer.parseInt( attribute.getValue() );
			}
		}
		
		return 0;		
	}	
}
	
	
