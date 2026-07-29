import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import axiosClient from '../../../services/axiosClient';
import './RaceSimulation.css';

const RaceSimulation = () => {
  const { user } = useSelector((state) => state.auth || {});
  const { raceId } = useParams();
  const navigate = useNavigate();

  const [race, setRace] = useState(null);
  const [horses, setHorses] = useState([]);
  
  // Simulation States
  // WAITING_FOR_START, COUNTDOWN, RACING, FINISHED, COMPLETED_VIEW, REPLAY_RACING
  const [status, setStatus] = useState('WAITING_FOR_START'); 
  const [countdown, setCountdown] = useState(3);
  const [realTimeCountdown, setRealTimeCountdown] = useState('');
  const [leaderboard, setLeaderboard] = useState([]);
  const [violations, setViolations] = useState([]);
  const [userBets, setUserBets] = useState([]);
  
  // Track horse progress (0 to 100)
  const [progress, setProgress] = useState({});
  
  // Refs
  const intervalRef = useRef(null);
  const clockRef = useRef(null);
  const hasFinalizedRef = useRef(false);
  
  const finishLineX = 90; // 90% of container width to leave space for the avatar

  useEffect(() => {
    fetchRaceData();
    if (user?.id) {
        fetchUserBets();
    }
    // Cleanup on unmount
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
      if (clockRef.current) clearInterval(clockRef.current);
    };
  }, [raceId, user?.id]);

  const fetchUserBets = async () => {
      try {
          const betsRes = await axiosClient.get(`/v1/spectator/bets/history/${user.id}`);
          const betsForThisRace = betsRes.filter(b => b.raceId === raceId);
          setUserBets(betsForThisRace);
      } catch (e) {
          console.error("Lỗi lấy lịch sử cược", e);
      }
  };

  useEffect(() => {
    if (race && race.startTime && status === 'WAITING_FOR_START') {
      const targetTime = new Date(race.startTime).getTime();
      
      clockRef.current = setInterval(() => {
        const now = new Date().getTime();
        const distance = targetTime - now;

        if (distance <= 0) {
          clearInterval(clockRef.current);
          setRealTimeCountdown('00:00:00');
          setStatus('READY');
        } else {
          // Calculate hours, minutes, seconds
          const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
          const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
          const seconds = Math.floor((distance % (1000 * 60)) / 1000);
          
          setRealTimeCountdown(
            `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
          );
        }
      }, 1000);
    }
    
    return () => {
      if (clockRef.current) clearInterval(clockRef.current);
    };
  }, [race, status]);

  const fetchRaceData = async () => {
    try {
      const raceRes = await axiosClient.get(`/v1/spectator/races`);
      const currentRace = raceRes?.find(r => r.id === raceId);
      setRace(currentRace);
      
      if (currentRace && currentRace.status === 'COMPLETED') {
          // Fetch results for REPLAY mode
          try {
              const results = await axiosClient.get(`/referee/race/${raceId}/results`);
              if (results && results.length > 0) {
                  const initialProgress = {};
                  // Sắp xếp lại danh sách ngựa hiển thị theo id hoặc random để lúc chạy không bị biết trước
                  const displayHorses = [...results].sort((a, b) => a.horseId.localeCompare(b.horseId));
                  displayHorses.forEach(h => {
                      initialProgress[h.horseId] = { 
                          x: 0, 
                          finished: false, 
                          time: h.finishTime, 
                          actualFinishTime: h.finishTime, // Lưu lại thời gian thật để giả lập tốc độ
                          horse: h 
                      };
                  });
                  setHorses(displayHorses);
                  setProgress(initialProgress);
                  // Load sẵn leaderboard
                  setLeaderboard([...results].sort((a, b) => a.position - b.position));
                  setStatus('COMPLETED_VIEW');
                  return;
              }
          } catch (e) {
              console.warn("Không lấy được kết quả để replay", e);
          }
      }

      // Live mode (not completed)
      try {
          const horsesRes = await axiosClient.get(`/referee/race/${raceId}/horses`);
          const initialProgress = {};
          horsesRes.forEach(h => {
              initialProgress[h.horseId] = { x: 0, finished: false, time: null, horse: h };
          });
          setHorses(horsesRes);
          setProgress(initialProgress);
      } catch (err) {
          console.warn("Could not fetch horses from referee endpoint, using mock data from general horses");
          const allHorses = await axiosClient.get(`/v1/horses`);
          const mockHorses = allHorses.slice(0, 5).map(h => ({ horseId: h.id, horseName: h.name, jockeyId: 'jockey-1', jockeyName: 'Nài Chưa Rõ' }));
          
          const initialProgress = {};
          mockHorses.forEach(h => {
              initialProgress[h.horseId] = { x: 0, finished: false, time: null, horse: h };
          });
          setHorses(mockHorses);
          setProgress(initialProgress);
      }
    } catch (error) {
      console.error("Lỗi khi tải dữ liệu đua:", error);
    }
  };

  const startReplay = () => {
    setStatus('COUNTDOWN');
    setCountdown(3);
    
    // Reset positions
    setProgress(prev => {
        const resetProgress = {};
        Object.keys(prev).forEach(k => {
            resetProgress[k] = { ...prev[k], x: 0, finished: false };
        });
        return resetProgress;
    });

    let currentCount = 3;
    const countInterval = setInterval(() => {
      currentCount -= 1;
      setCountdown(currentCount);
      
      if (currentCount === 0) {
        clearInterval(countInterval);
        setStatus('REPLAY_RACING');
        runReplayRacing();
      }
    }, 1000);
  };

  const runReplayRacing = () => {
    // 100ms = 0.1s per frame
    const frameRate = 0.1;
    
    intervalRef.current = setInterval(() => {
        setProgress(prev => {
            const newProgress = { ...prev };
            let allFinished = true;
            
            Object.keys(newProgress).forEach(hId => {
                const horseState = newProgress[hId];
                
                // Ngựa bị loại thì coi như chạy xong luôn (đứng yên), không đợi 999s
                if (horseState.horse && horseState.horse.position === 99) {
                    horseState.finished = true;
                }
                
                if (!horseState.finished) {
                    allFinished = false;
                    
                    // Tính tốc độ sao cho đúng actualFinishTime thì tới đích (finishLineX)
                    // Quãng đường trong 0.1s = (Tổng quãng đường / Tổng thời gian) * 0.1
                    const speed = (finishLineX / horseState.actualFinishTime) * frameRate;
                    let newX = horseState.x + speed;
                    
                    if (newX >= finishLineX) {
                        newX = finishLineX;
                        horseState.finished = true;
                    }
                    
                    horseState.x = newX;
                }
            });
            
            if (allFinished) {
                clearInterval(intervalRef.current);
                setStatus('COMPLETED_VIEW');
            }
            
            return newProgress;
        });
    }, 100);
  };

  const startSimulation = () => {
    if (horses.length === 0) {
        console.warn("Không có ngựa nào tham gia!");
        return;
    }
    
    setStatus('COUNTDOWN');
    setCountdown(3);
    setLeaderboard([]);
    setViolations([]);
    hasFinalizedRef.current = false;
    
    let currentCount = 3;
    
    const countInterval = setInterval(() => {
      currentCount -= 1;
      setCountdown(currentCount);
      
      if (currentCount === 0) {
        clearInterval(countInterval);
        setStatus('RACING');
        startRacing();
      }
    }, 1000);
  };

  const reportFalseStart = async (horseData) => {
      const violationMsg = `Lỗi XUẤT PHÁT SỚM: Chiến mã ${horseData.horseName} đã cố tình phá rào chạy trước khi có hiệu lệnh.`;
      setViolations(prev => [...prev, violationMsg]);
      
      try {
          await axiosClient.post(`/referee/race/${raceId}/violation`, {
              horseId: horseData.horseId,
              jockeyId: horseData.jockeyId,
              violationType: 'FALSE_START',
              description: violationMsg,
              penalty: 500000,
              severity: 'HIGH',
              refereeId: 'system_auto'
          });
      } catch (err) {
          console.error("Gửi vi phạm thất bại", err);
      }
  };

  const startRacing = () => {
    const startTime = Date.now();
    let currentLeaderboard = [];
    
    intervalRef.current = setInterval(() => {
        setProgress(prev => {
            const newProgress = { ...prev };
            let allFinished = true;
            
            Object.keys(newProgress).forEach(hId => {
                const horseState = newProgress[hId];
                
                if (!horseState.finished) {
                    allFinished = false;
                    const speed = Math.random() * 0.2 + 0.05; 
                    let newX = horseState.x + speed;
                    
                    if (newX >= finishLineX) {
                        newX = finishLineX;
                        horseState.finished = true;
                        horseState.time = ((Date.now() - startTime) / 1000).toFixed(2);
                        
                        // Add to leaderboard
                        currentLeaderboard.push({
                            ...horseState.horse,
                            finishTime: horseState.time,
                            position: currentLeaderboard.length + 1
                        });
                        setLeaderboard([...currentLeaderboard]);
                    }
                    
                    horseState.x = newX;
                }
            });
            
            if (allFinished) {
                clearInterval(intervalRef.current);
                if (!hasFinalizedRef.current) {
                    hasFinalizedRef.current = true;
                    setStatus('FINISHED');
                    finalizeRace(currentLeaderboard);
                } else {
                    setStatus('FINISHED');
                }
            }
            
            return newProgress;
        });
    }, 100);
  };

  const finalizeRace = async (finalBoard) => {
      try {
          // 1. Submit results for all horses
          for (const result of finalBoard) {
              await axiosClient.post(`/referee/race/${raceId}/result`, {
                  horseId: result.horseId,
                  jockeyId: result.jockeyId,
                  position: result.position,
                  finishTime: parseFloat(result.finishTime)
              });
          }
          
          // 2. Thông báo chờ Trọng Tài duyệt
          alert("🏆 Cuộc đua đã kết thúc! Kết quả thô đã được gửi lên hệ thống. \nXin chờ Trọng tài xem xét vi phạm và chốt kết quả cuối cùng để chia thưởng!");
      } catch (error) {
          console.error("Lỗi khi gửi kết quả:", error);
          alert("Lỗi khi gửi kết quả thô lên hệ thống!");
      }
  };

  const renderHorseEmoji = (index) => {
      const emojis = ['🐎', '🐴', '🦄', '🏇'];
      return emojis[index % emojis.length];
  };

  return (
    <div className="rs-container">
      <div className="rs-header">
        <h2>Trường Đua EquineElite Giả Lập</h2>
        <div className="rs-status">
            {status === 'WAITING_FOR_START' && '⏳ Đang chờ thời gian khởi tranh...'}
            {status === 'COUNTDOWN' && '🔥 Đếm ngược...'}
            {(status === 'RACING' || status === 'REPLAY_RACING') && '🔴 Đang trực tiếp đua!'}
            {status === 'FINISHED' && '🏁 Cuộc đua đã kết thúc!'}
            {status === 'COMPLETED_VIEW' && '🏁 Xem lại diễn biến cuộc đua'}
        </div>
        
        {status === 'WAITING_FOR_START' && (
            <div className="rs-countdown-display" style={{ color: '#3b82f6' }}>
              {realTimeCountdown || '--:--:--'}
            </div>
        )}

        {status === 'READY' && (
            <div style={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                <div className="rs-countdown-display rs-pulse" style={{ color: '#10b981', marginBottom: '10px' }}>
                Đã đến giờ G!
                </div>
                <button className="rs-start-button" onClick={startSimulation}>
                    Phất Cờ Xuất Phát (Quyền Trọng Tài)
                </button>
            </div>
        )}

        {status === 'COUNTDOWN' && (
            <div className="rs-countdown-display rs-pulse">{countdown}</div>
        )}
        
        {status === 'FINISHED' && (
            <button className="rs-start-button" onClick={() => navigate(-1)} style={{ background: '#3b82f6' }}>
                Quay Lại Lịch Đua
            </button>
        )}

        {status === 'COMPLETED_VIEW' && (
            <div style={{ display: 'flex', gap: '15px', justifyContent: 'center' }}>
                <button className="rs-start-button" onClick={startReplay} style={{ background: '#f59e0b', color: 'white' }}>
                    ▶ Phát Lại Cuộc Đua
                </button>
                <button className="rs-start-button" onClick={() => navigate(-1)} style={{ background: '#3b82f6' }}>
                    Quay Lại Lịch Đua
                </button>
            </div>
        )}
      </div>

      <div className="rs-track-container">
        <div className="rs-finish-line"></div>
        
        {horses.map((horse, index) => {
            const hState = progress[horse.horseId] || { x: 0 };
            const isUserBet = userBets.some(b => b.horseId === horse.horseId);
            return (
                <div key={horse.horseId} className="rs-horse-lane">
                    <div 
                        className={`rs-horse-avatar ${hState.isFalseStart ? 'rs-false-start' : ''} ${horse.position === 99 ? 'rs-disqualified' : ''}`}
                        style={{ left: `${hState.x}%`, opacity: horse.position === 99 ? 0.5 : 1, filter: horse.position === 99 ? 'grayscale(100%)' : 'none' }}
                    >
                        {horse.position === 99 ? '❌' : renderHorseEmoji(index)}
                        <span className="rs-horse-info-badge" style={{ textDecoration: horse.position === 99 ? 'line-through' : 'none', background: horse.position === 99 ? '#ef4444' : isUserBet ? '#10b981' : '' }}>
                            {horse.horseName} {isUserBet ? '(Bạn cược)' : ''}
                        </span>
                    </div>
                </div>
            );
        })}
      </div>

      {violations.length > 0 && (
          <div style={{ marginBottom: '30px' }}>
              {violations.map((v, i) => (
                  <div key={i} className="rs-violation-alert">
                      <strong>⚠️ PHẠT NGUỘI:</strong> {v}
                  </div>
              ))}
          </div>
      )}

      {leaderboard.length > 0 && (
          <div className="rs-leaderboard">
              <h3>🏆 Bảng Phong Thần Chung Cuộc</h3>
              <ul className="rs-leaderboard-list">
                  {leaderboard.map((item) => {
                      const isUserBet = userBets.some(b => b.horseId === item.horseId);
                      return (
                      <li key={item.horseId} className="rs-leaderboard-item" style={isUserBet ? { borderLeft: '4px solid #10b981', backgroundColor: '#f0fdf4' } : {}}>
                          <div style={{ display: 'flex', alignItems: 'center' }}>
                              <span className={`rs-rank-badge ${item.position === 99 ? 'rs-rank-disqualified' : `rs-rank-${item.position > 3 ? 'other' : item.position}`}`} style={item.position === 99 ? {background: '#ef4444', color: 'white'} : {}}>
                                  {item.position === 99 ? '❌' : item.position}
                              </span>
                              <strong style={{ fontSize: '1.1rem', color: item.position === 99 ? '#9ca3af' : '#111827', textDecoration: item.position === 99 ? 'line-through' : 'none' }}>
                                  {item.horseName} 
                                  {isUserBet && <span style={{ color: '#10b981', fontSize: '0.8rem', marginLeft: '5px', fontWeight: 'bold' }}>⭐ BẠN ĐÃ CƯỢC</span>}
                              </strong>
                              <span style={{ color: '#64748b', marginLeft: '10px', fontSize: '0.95rem' }}>
                                  (Nài: {item.jockeyName || 'Chưa rõ'})
                              </span>
                          </div>
                          <span className="rs-finish-time" style={item.position === 99 ? {color: '#ef4444', fontWeight: 'bold'} : {}}>
                              {item.position === 99 ? 'BỊ LOẠI' : `${item.finishTime}s`}
                          </span>
                      </li>
                      );
                  })}
              </ul>
          </div>
      )}
    </div>
  );
};

export default RaceSimulation;
