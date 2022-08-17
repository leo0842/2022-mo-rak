import styled from '@emotion/styled';
import React from 'react';
import MainFeatureMenuContainer from '../../components/Main/MainFeatureMenuContainer/MainFeatureMenuContainer';
import { useMenuDispatch } from '../../context/MenuProvider';

function MainPage() {
  const dispatch = useMenuDispatch();
  dispatch({ type: 'SET_CLICKED_MENU', menu: 'main' });

  return (
    <StyledContainer>
      <MainFeatureMenuContainer />
    </StyledContainer>
  );
}

const StyledContainer = styled.div`
  width: calc(100% - 36.4rem);
  padding: 6.4rem 20rem;
`;

export default MainPage;
